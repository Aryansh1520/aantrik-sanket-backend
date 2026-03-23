package com.aantriksanket.backend.api.tenant.clients;

import com.aantriksanket.backend.models.Client;
import com.aantriksanket.backend.models.ClientStatus;
import com.aantriksanket.backend.models.Gender;
import com.aantriksanket.backend.models.MaritalStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class ClientResponse {

    private UUID id;
    private UUID tenantId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private Gender gender;
    private String occupation;
    private MaritalStatus maritalStatus;
    private String educationLevel;
    private String nationality;
    private String generalProblems;
    private String additionalNotes;
    private String address;
    private String emergencyContactNumber;
    private Map<String, Object> questionsAnswers;
    private OffsetDateTime firstSessionDate;
    private OffsetDateTime lastSessionDate;
    private OffsetDateTime upcomingSessionDate;
    private String sessionRecurrence;
    private ClientStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ClientResponse(Client client) {
        this.id = client.getId();
        this.tenantId = client.getTenant() != null ? client.getTenant().getId() : null;
        this.fullName = client.getFullName();
        this.email = client.getEmail();
        this.phoneNumber = client.getPhoneNumber();
        this.dob = client.getDob();
        this.gender = client.getGender();
        this.occupation = client.getOccupation();
        this.maritalStatus = client.getMaritalStatus();
        this.educationLevel = client.getEducationLevel();
        this.nationality = client.getNationality();
        this.generalProblems = client.getGeneralProblems();
        this.additionalNotes = client.getAdditionalNotes();
        this.address = client.getAddress();
        this.emergencyContactNumber = client.getEmergencyContactNumber();
        this.questionsAnswers = client.getQuestionsAnswers();
        this.firstSessionDate = client.getFirstSessionDate();
        this.lastSessionDate = client.getLastSessionDate();
        this.upcomingSessionDate = client.getUpcomingSessionDate();
        this.sessionRecurrence = client.getSessionRecurrence();
        this.status = client.getStatus();
        this.createdAt = client.getCreatedAt();
        this.updatedAt = client.getUpdatedAt();
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDob() { return dob; }
    public Gender getGender() { return gender; }
    public String getOccupation() { return occupation; }
    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public String getEducationLevel() { return educationLevel; }
    public String getNationality() { return nationality; }
    public String getGeneralProblems() { return generalProblems; }
    public String getAdditionalNotes() { return additionalNotes; }
    public String getAddress() { return address; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public Map<String, Object> getQuestionsAnswers() { return questionsAnswers; }
    public OffsetDateTime getFirstSessionDate() { return firstSessionDate; }
    public OffsetDateTime getLastSessionDate() { return lastSessionDate; }
    public OffsetDateTime getUpcomingSessionDate() { return upcomingSessionDate; }
    public String getSessionRecurrence() { return sessionRecurrence; }
    public ClientStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
