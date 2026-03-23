package com.aantriksanket.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "clients", indexes = {
    @Index(name = "idx_client_email", columnList = "email"),
    @Index(name = "idx_client_tenant_id", columnList = "tenant_id")
})
public class Client {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "gender_enum")
    private Gender gender;

    @Column(name = "occupation", length = 255)
    private String occupation;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "marital_status", columnDefinition = "marital_status_enum")
    private MaritalStatus maritalStatus;

    @Column(name = "education_level", length = 255)
    private String educationLevel;

    @Column(name = "nationality", length = 255)
    private String nationality;

    @Column(name = "general_problems", columnDefinition = "TEXT")
    private String generalProblems;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "consent_form", columnDefinition = "bytea")
    private byte[] consentForm;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "emergency_contact_number", length = 255)
    private String emergencyContactNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "questions_answers", columnDefinition = "jsonb")
    private Map<String, Object> questionsAnswers;

    @Column(name = "first_session_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime firstSessionDate;

    @Column(name = "last_session_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastSessionDate;

    @Column(name = "upcoming_session_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime upcomingSessionDate;

    @Column(name = "session_recurrence", length = 255)
    private String sessionRecurrence;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "client_status_enum")
    private ClientStatus status = ClientStatus.NEW;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    public Client() {
    }

    public Client(Tenant tenant, String fullName, String email, String phoneNumber) {
        this.tenant = tenant;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.status = ClientStatus.NEW;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getGeneralProblems() {
        return generalProblems;
    }

    public void setGeneralProblems(String generalProblems) {
        this.generalProblems = generalProblems;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public byte[] getConsentForm() {
        return consentForm;
    }

    public void setConsentForm(byte[] consentForm) {
        this.consentForm = consentForm;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    public Map<String, Object> getQuestionsAnswers() {
        return questionsAnswers;
    }

    public void setQuestionsAnswers(Map<String, Object> questionsAnswers) {
        this.questionsAnswers = questionsAnswers;
    }

    public OffsetDateTime getFirstSessionDate() {
        return firstSessionDate;
    }

    public void setFirstSessionDate(OffsetDateTime firstSessionDate) {
        this.firstSessionDate = firstSessionDate;
    }

    public OffsetDateTime getLastSessionDate() {
        return lastSessionDate;
    }

    public void setLastSessionDate(OffsetDateTime lastSessionDate) {
        this.lastSessionDate = lastSessionDate;
    }

    public OffsetDateTime getUpcomingSessionDate() {
        return upcomingSessionDate;
    }

    public void setUpcomingSessionDate(OffsetDateTime upcomingSessionDate) {
        this.upcomingSessionDate = upcomingSessionDate;
    }

    public String getSessionRecurrence() {
        return sessionRecurrence;
    }

    public void setSessionRecurrence(String sessionRecurrence) {
        this.sessionRecurrence = sessionRecurrence;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
