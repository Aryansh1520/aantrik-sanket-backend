package com.aantriksanket.backend.service.sessions;

import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.sessions.CalendarEvent;
import com.aantriksanket.backend.models.sessions.CalendarEventRepository;
import com.aantriksanket.backend.models.sessions.Session;
import com.aantriksanket.backend.models.sessions.SessionRepository;
import com.aantriksanket.backend.models.sessions.SessionType;
import com.aantriksanket.backend.models.sessions.SyncStatus;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CalendarSyncJob {

    private final SessionRepository sessionRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final GoogleCredentialService googleCredentialService;

    public CalendarSyncJob(SessionRepository sessionRepository,
                           CalendarEventRepository calendarEventRepository,
                           GoogleCredentialService googleCredentialService) {
        this.sessionRepository = sessionRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.googleCredentialService = googleCredentialService;
    }

    private Calendar getCalendarService(Tenant tenant) throws GeneralSecurityException, IOException {
        Credential credential = googleCredentialService.getCredential(tenant);
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Aantrik Sanket")
                .build();
    }

    @Transactional
    public void syncCreateEvent(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || "CANCELED".equals(session.getStatus().name())) return;
        
        CalendarEvent ce = calendarEventRepository.findById(session.getId()).orElse(null);
        if (ce == null || ce.getSyncStatus() == SyncStatus.SYNCED) return; // Idempotent check

        try {
            Calendar service = getCalendarService(session.getTherapist());
            Event event = new Event()
                .setSummary("Therapy Session - " + session.getClient().getFullName())
                .setDescription("Status: " + session.getStatus() + "\nClient: " + session.getClient().getFullName());

            if (session.getLocation() != null) {
                event.setLocation(session.getLocation());
            }

            DateTime startDateTime = new DateTime(session.getStartTime().toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime().setDateTime(startDateTime);
            event.setStart(start);

            DateTime endDateTime = new DateTime(session.getEndTime().toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime().setDateTime(endDateTime);
            event.setEnd(end);

            boolean isGoogleMeet = session.getType() == SessionType.GOOGLE_MEET;
            
            // Only add conference data if we need a meet link
            if (isGoogleMeet) {
                ConferenceSolutionKey solutionKey = new ConferenceSolutionKey().setType("hangoutsMeet");
                CreateConferenceRequest createRequest = new CreateConferenceRequest()
                        .setRequestId(session.getId().toString())
                        .setConferenceSolutionKey(solutionKey);
                ConferenceData conferenceData = new ConferenceData().setCreateRequest(createRequest);
                event.setConferenceData(conferenceData);
            }

            Event createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(isGoogleMeet ? 1 : 0)
                    .execute();

            ce.setExternalEventId(createdEvent.getId());
            ce.setSyncStatus(SyncStatus.SYNCED);
            ce.setLastSyncedAt(OffsetDateTime.now());
            
            if (isGoogleMeet && createdEvent.getConferenceData() != null) {
                String meetLink = createdEvent.getConferenceData().getEntryPoints().stream()
                        .filter(ep -> "video".equals(ep.getEntryPointType()))
                        .map(ep -> ep.getUri())
                        .findFirst().orElse(null);
                
                ce.setMeetLink(meetLink);
                session.setMeetLink(meetLink);
                // Propagate to Series if it's the master
                if (session.getIsSeriesMaster() && session.getSeries() != null) {
                    session.getSeries().setMeetLink(meetLink);
                }
            }
            
            calendarEventRepository.save(ce);
            sessionRepository.save(session);
        } catch (Exception e) {
            ce.setSyncStatus(SyncStatus.FAILED);
            ce.setErrorMessage(e.getMessage());
            calendarEventRepository.save(ce);
            throw new RuntimeException("Sync create failed for session " + sessionId, e); // To allow JobRunr to retry
        }
    }

    @Transactional
    public void syncPatchEvent(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return;
        
        CalendarEvent ce = calendarEventRepository.findById(session.getId()).orElse(null);
        if (ce == null || ce.getExternalEventId() == null) return;

        try {
            Calendar service = getCalendarService(session.getTherapist());
            Event event = service.events().get("primary", ce.getExternalEventId()).execute();
            
            if (event.getStatus().equals("cancelled")) return; // Cannot update cancelled event
            
            event.setSummary("Therapy Session - " + session.getClient().getFullName() + " [" + session.getStatus() + "]");
            
            if (session.getLocation() != null) event.setLocation(session.getLocation());
            
            DateTime startDateTime = new DateTime(session.getStartTime().toInstant().toEpochMilli());
            event.setStart(new EventDateTime().setDateTime(startDateTime));

            DateTime endDateTime = new DateTime(session.getEndTime().toInstant().toEpochMilli());
            event.setEnd(new EventDateTime().setDateTime(endDateTime));
            
            service.events().update("primary", ce.getExternalEventId(), event).execute();
            
            ce.setSyncStatus(SyncStatus.SYNCED);
            ce.setLastSyncedAt(OffsetDateTime.now());
            ce.setErrorMessage(null);
            calendarEventRepository.save(ce);
        } catch (Exception e) {
            ce.setSyncStatus(SyncStatus.FAILED);
            ce.setErrorMessage(e.getMessage());
            calendarEventRepository.save(ce);
            throw new RuntimeException("Sync patch failed for session " + sessionId, e);
        }
    }

    @Transactional
    public void syncDeleteEvent(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return;
        
        CalendarEvent ce = calendarEventRepository.findById(session.getId()).orElse(null);
        if (ce == null || ce.getExternalEventId() == null) return;

        try {
            Calendar service = getCalendarService(session.getTherapist());
            service.events().delete("primary", ce.getExternalEventId()).execute();
            
            ce.setSyncStatus(SyncStatus.SYNCED);
            ce.setLastSyncedAt(OffsetDateTime.now());
            ce.setErrorMessage(null);
            calendarEventRepository.save(ce);
        } catch (Exception e) {
            // 404 means already deleted
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                ce.setSyncStatus(SyncStatus.SYNCED);
                calendarEventRepository.save(ce);
                return;
            }
            ce.setSyncStatus(SyncStatus.FAILED);
            ce.setErrorMessage(e.getMessage());
            calendarEventRepository.save(ce);
            throw new RuntimeException("Sync delete failed for session " + sessionId, e);
        }
    }
}
