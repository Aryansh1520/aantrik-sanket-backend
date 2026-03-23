package com.aantriksanket.backend.service.sessions;

import com.aantriksanket.backend.api.tenant.sessions.ConflictDetail;
import com.aantriksanket.backend.api.tenant.sessions.CreateSessionRequest;
import com.aantriksanket.backend.api.tenant.sessions.SessionResponse;
import com.aantriksanket.backend.api.tenant.sessions.UpdateSessionRequest;
import com.aantriksanket.backend.models.Client;
import com.aantriksanket.backend.models.ClientRepository;
import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.sessions.*;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SeriesRepository seriesRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final ClientRepository clientRepository;
    private final JobScheduler jobScheduler;
    private final CalendarSyncJob calendarSyncJob;

    public SessionService(SessionRepository sessionRepository,
                          SeriesRepository seriesRepository,
                          CalendarEventRepository calendarEventRepository,
                          ClientRepository clientRepository,
                          JobScheduler jobScheduler,
                          CalendarSyncJob calendarSyncJob) {
        this.sessionRepository = sessionRepository;
        this.seriesRepository = seriesRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.clientRepository = clientRepository;
        this.jobScheduler = jobScheduler;
        this.calendarSyncJob = calendarSyncJob;
    }

    public static class ConflictException extends RuntimeException {
        private final List<ConflictDetail> conflicts;
        public ConflictException(List<ConflictDetail> conflicts) {
            super("Scheduling conflict detected");
            this.conflicts = conflicts;
        }
        public List<ConflictDetail> getConflicts() { return conflicts; }
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) { super(message); }
    }

    private void checkConflicts(Tenant therapist, List<OffsetDateTime> startTimes, Integer durationMinutes, List<UUID> excludeSessionIds) {
        OffsetDateTime minStart = startTimes.stream().min(OffsetDateTime::compareTo).orElseThrow();
        OffsetDateTime maxEnd = startTimes.stream().max(OffsetDateTime::compareTo).orElseThrow().plusMinutes(durationMinutes);

        List<Session> periodSessions = sessionRepository.findConflicts(therapist.getId(), excludeSessionIds, minStart, maxEnd);
        
        List<ConflictDetail> conflicts = new ArrayList<>();
        for (OffsetDateTime proposedStart : startTimes) {
            OffsetDateTime proposedEnd = proposedStart.plusMinutes(durationMinutes);
            for (Session existing : periodSessions) {
                if (existing.getStartTime().isBefore(proposedEnd) && existing.getEndTime().isAfter(proposedStart)) {
                    conflicts.add(new ConflictDetail(existing));
                }
            }
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException(conflicts);
        }
    }

    @Transactional
    public List<SessionResponse> createSession(Tenant therapist, CreateSessionRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ValidationException("Client not found"));
        
        if (!client.getTenant().getId().equals(therapist.getId())) {
            throw new ValidationException("Client does not belong to this therapist");
        }
        
        if (request.getAnchorStartTime().isBefore(OffsetDateTime.now())) {
            throw new ValidationException("Cannot schedule a session in the past");
        }

        boolean isSeries = request.getFrequencyDays() != null && request.getTotalCount() != null;
        int count = isSeries ? request.getTotalCount() : 1;
        
        List<OffsetDateTime> proposedStartTimes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int offsetDays = isSeries ? request.getFrequencyDays() * i : 0;
            proposedStartTimes.add(request.getAnchorStartTime().plusDays(offsetDays));
        }

        checkConflicts(therapist, proposedStartTimes, request.getDurationMinutes(), null);

        Series series = null;
        if (isSeries) {
            series = new Series();
            series.setClient(client);
            series.setTherapist(therapist);
            series.setFrequencyDays(request.getFrequencyDays());
            series.setTotalCount(request.getTotalCount());
            series.setAnchorStartTime(request.getAnchorStartTime());
            series.setDurationMinutes(request.getDurationMinutes());
            series.setType(SessionType.valueOf(request.getType()));
            series.setPrice(request.getPrice());
            series.setLocation(request.getLocation());
            seriesRepository.save(series);
        }

        List<Session> createdSessions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Session session = new Session();
            session.setClient(client);
            session.setTherapist(therapist);
            session.setSeries(series);
            session.setIsSeriesMaster(i == 0);
            session.setStartTime(proposedStartTimes.get(i));
            session.setEndTime(proposedStartTimes.get(i).plusMinutes(request.getDurationMinutes()));
            session.setDurationMinutes(request.getDurationMinutes());
            session.setType(SessionType.valueOf(request.getType()));
            session.setPrice(request.getPrice());
            session.setLocation(request.getLocation());
            session.setOriginalStartTime(session.getStartTime());
            session.setOriginalEndTime(session.getEndTime());
            sessionRepository.save(session);
            
            CalendarEvent ce = new CalendarEvent();
            ce.setId(session.getId());
            ce.setSession(session);
            ce.setProvider("google");
            calendarEventRepository.save(ce);
            
            createdSessions.add(session);
            
            // Dispatch Jobrunr sync
            jobScheduler.enqueue(() -> calendarSyncJob.syncCreateEvent(session.getId()));
        }

        return createdSessions.stream()
                .map(s -> new SessionResponse(s, SyncStatus.PENDING))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessions(Tenant therapist, UUID clientId, Boolean includeCanceled) {
        // A simple implementation. Real-world would use Specification or QueryDSL
        return sessionRepository.findAll().stream()
                .filter(s -> s.getTherapist().getId().equals(therapist.getId()))
                .filter(s -> clientId == null || s.getClient().getId().equals(clientId))
                .filter(s -> Boolean.TRUE.equals(includeCanceled) || !"CANCELED".equals(s.getStatus().name()))
                .map(s -> {
                    CalendarEvent ce = calendarEventRepository.findById(s.getId()).orElse(null);
                    return new SessionResponse(s, ce != null ? ce.getSyncStatus() : SyncStatus.PENDING);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Tenant therapist, UUID sessionId) {
        Session s = sessionRepository.findById(sessionId).orElseThrow(() -> new ValidationException("Session not found"));
        if (!s.getTherapist().getId().equals(therapist.getId())) {
            throw new ValidationException("Not authorized");
        }
        CalendarEvent ce = calendarEventRepository.findById(s.getId()).orElse(null);
        return new SessionResponse(s, ce != null ? ce.getSyncStatus() : SyncStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSeriesSessions(Tenant therapist, UUID seriesId) {
        return sessionRepository.findBySeriesIdOrderByStartTimeAsc(seriesId).stream()
                .filter(s -> s.getTherapist().getId().equals(therapist.getId()))
                .map(s -> {
                    CalendarEvent ce = calendarEventRepository.findById(s.getId()).orElse(null);
                    return new SessionResponse(s, ce != null ? ce.getSyncStatus() : SyncStatus.PENDING);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SessionResponse> patchSession(Tenant therapist, UUID sessionId, String scope, UpdateSessionRequest req) {
        Session target = sessionRepository.findById(sessionId).orElseThrow(() -> new ValidationException("Session not found"));
        if (!target.getTherapist().getId().equals(therapist.getId())) throw new ValidationException("Not authorized");
        if (target.getStartTime().isBefore(OffsetDateTime.now())) throw new ValidationException("Cannot modify past session");
        
        boolean futureInSeries = "future_in_series".equalsIgnoreCase(scope);
        List<Session> toUpdate = new ArrayList<>();
        
        if (futureInSeries && target.getSeries() != null) {
            toUpdate = sessionRepository.findBySeriesIdOrderByStartTimeAsc(target.getSeries().getId()).stream()
                    .filter(s -> !s.getStartTime().isBefore(target.getStartTime()))
                    .filter(s -> !"CANCELED".equals(s.getStatus().name()))
                    .collect(Collectors.toList());
        } else {
            toUpdate.add(target);
        }

        // Conflict check
        if (req.getStartTime() != null || req.getDurationMinutes() != null) {
            List<OffsetDateTime> proposedTimes = new ArrayList<>();
            int newDuration = req.getDurationMinutes() != null ? req.getDurationMinutes() : target.getDurationMinutes();
            
            OffsetDateTime timeShift = req.getStartTime() != null ? req.getStartTime() : target.getStartTime();
            long offsetMillis = req.getStartTime() != null ? 
                    req.getStartTime().toInstant().toEpochMilli() - target.getStartTime().toInstant().toEpochMilli() : 0;
            
            for (Session s : toUpdate) {
                OffsetDateTime proposedStart = OffsetDateTime.ofInstant(s.getStartTime().toInstant().plusMillis(offsetMillis), s.getStartTime().getOffset());
                proposedTimes.add(proposedStart);
            }
            List<UUID> excludeIds = toUpdate.stream().map(Session::getId).collect(Collectors.toList());
            checkConflicts(therapist, proposedTimes, newDuration, excludeIds);
        }

        // Apply
        long offsetMillis = req.getStartTime() != null ? 
                req.getStartTime().toInstant().toEpochMilli() - target.getStartTime().toInstant().toEpochMilli() : 0;

        for (Session s : toUpdate) {
            if (req.getType() != null) s.setType(SessionType.valueOf(req.getType()));
            if (req.getPrice() != null) s.setPrice(req.getPrice());
            if (req.getLocation() != null) s.setLocation(req.getLocation());
            if (req.getDurationMinutes() != null) {
                s.setDurationMinutes(req.getDurationMinutes());
                s.setEndTime(s.getStartTime().plusMinutes(req.getDurationMinutes()));
            }
            if (req.getStartTime() != null) {
                OffsetDateTime newStart = OffsetDateTime.ofInstant(s.getStartTime().toInstant().plusMillis(offsetMillis), s.getStartTime().getOffset());
                s.setStartTime(newStart);
                s.setEndTime(newStart.plusMinutes(s.getDurationMinutes()));
                s.setStatus(SessionStatus.RESCHEDULED);
                s.setRescheduleReason("Patch update");
            }
            sessionRepository.save(s);
            jobScheduler.enqueue(() -> calendarSyncJob.syncPatchEvent(s.getId()));
        }

        if (futureInSeries && target.getSeries() != null) {
            Series series = target.getSeries();
            if (req.getType() != null) series.setType(SessionType.valueOf(req.getType()));
            if (req.getPrice() != null) series.setPrice(req.getPrice());
            if (req.getLocation() != null) series.setLocation(req.getLocation());
            if (req.getDurationMinutes() != null) series.setDurationMinutes(req.getDurationMinutes());
            seriesRepository.save(series);
        }

        return toUpdate.stream().map(s -> {
                    CalendarEvent ce = calendarEventRepository.findById(s.getId()).orElse(null);
                    return new SessionResponse(s, ce != null ? ce.getSyncStatus() : SyncStatus.PENDING);
                }).collect(Collectors.toList());
    }

    @Transactional
    public List<SessionResponse> cancelSession(Tenant therapist, UUID sessionId, String scope) {
        Session target = sessionRepository.findById(sessionId).orElseThrow(() -> new ValidationException("Session not found"));
        if (!target.getTherapist().getId().equals(therapist.getId())) throw new ValidationException("Not authorized");
        if (target.getStartTime().isBefore(OffsetDateTime.now())) throw new ValidationException("Cannot cancel past session");
        if ("CANCELED".equals(target.getStatus().name())) throw new ValidationException("Session already canceled");

        boolean futureInSeries = "future_in_series".equalsIgnoreCase(scope);
        List<Session> toCancel = new ArrayList<>();
        if (futureInSeries && target.getSeries() != null) {
            toCancel = sessionRepository.findBySeriesIdOrderByStartTimeAsc(target.getSeries().getId()).stream()
                    .filter(s -> !s.getStartTime().isBefore(target.getStartTime()))
                    .filter(s -> !"CANCELED".equals(s.getStatus().name()))
                    .collect(Collectors.toList());
        } else {
            toCancel.add(target);
        }

        for (Session s : toCancel) {
            s.setStatus(SessionStatus.CANCELED);
            s.setCanceledAt(OffsetDateTime.now());
            sessionRepository.save(s);
            jobScheduler.enqueue(() -> calendarSyncJob.syncDeleteEvent(s.getId()));
        }

        // if all sessions in series are canceled, cancel series
        if (target.getSeries() != null) {
            boolean anyActive = sessionRepository.findBySeriesIdOrderByStartTimeAsc(target.getSeries().getId()).stream()
                .anyMatch(s -> !"CANCELED".equals(s.getStatus().name()));
            if (!anyActive) {
                Series series = target.getSeries();
                series.setStatus(SeriesStatus.CANCELED);
                seriesRepository.save(series);
            }
        }

        return toCancel.stream().map(s -> {
                    CalendarEvent ce = calendarEventRepository.findById(s.getId()).orElse(null);
                    return new SessionResponse(s, ce != null ? ce.getSyncStatus() : SyncStatus.PENDING);
                }).collect(Collectors.toList());
    }
}
