package com.aantriksanket.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenant_email", columnList = "email"),
    @Index(name = "idx_tenant_phone", columnList = "phone_number"),
    @Index(name = "idx_tenant_domain", columnList = "domain_name")
})
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "domain_name", nullable = false, unique = true)
    private String domainName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "subscription_validity", nullable = false)
    private Integer subscriptionValidity = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false, length = 20)
    private SubscriptionInterval subscriptionType = SubscriptionInterval.MONTHLY;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "first_logged_in", nullable = false)
    private Boolean firstLoggedIn = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @Column(name = "subscription_start_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime subscriptionStartAt;

    @Column(name = "subscription_end_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime subscriptionEndAt;

    @Column(name = "subscription_days_left", nullable = false)
    private Integer subscriptionDaysLeft = 0;

    @Column(name = "google_email")
    private String googleEmail;

    @Column(name = "google_calendar_id", columnDefinition = "VARCHAR(255) DEFAULT 'primary'")
    private String googleCalendarId = "primary";

    @Column(name = "google_access_token")
    private String googleAccessToken;

    @Column(name = "google_refresh_token")
    private String googleRefreshToken;

    @Column(name = "google_token_expiry", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime googleTokenExpiry;

    public Tenant() {
    }

    public Tenant(String firstName, String lastName, String phoneNumber, String email,
                 String passwordHash, String domainName, SubscriptionPlan subscriptionPlan) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.domainName = domainName;
        this.subscriptionPlan = subscriptionPlan;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public Integer getSubscriptionValidity() {
        return subscriptionValidity;
    }

    public void setSubscriptionValidity(Integer subscriptionValidity) {
        this.subscriptionValidity = subscriptionValidity;
    }

    public SubscriptionInterval getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionInterval subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getFirstLoggedIn() {
        return firstLoggedIn;
    }

    public void setFirstLoggedIn(Boolean firstLoggedIn) {
        this.firstLoggedIn = firstLoggedIn;
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

    public OffsetDateTime getSubscriptionStartAt() {
        return subscriptionStartAt;
    }

    public void setSubscriptionStartAt(OffsetDateTime subscriptionStartAt) {
        this.subscriptionStartAt = subscriptionStartAt;
    }

    public OffsetDateTime getSubscriptionEndAt() {
        return subscriptionEndAt;
    }

    public void setSubscriptionEndAt(OffsetDateTime subscriptionEndAt) {
        this.subscriptionEndAt = subscriptionEndAt;
    }

    public Integer getSubscriptionDaysLeft() {
        return subscriptionDaysLeft;
    }

    public void setSubscriptionDaysLeft(Integer subscriptionDaysLeft) {
        this.subscriptionDaysLeft = subscriptionDaysLeft;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
    }

    public String getGoogleCalendarId() {
        return googleCalendarId;
    }

    public void setGoogleCalendarId(String googleCalendarId) {
        this.googleCalendarId = googleCalendarId;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public String getGoogleRefreshToken() {
        return googleRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public OffsetDateTime getGoogleTokenExpiry() {
        return googleTokenExpiry;
    }

    public void setGoogleTokenExpiry(OffsetDateTime googleTokenExpiry) {
        this.googleTokenExpiry = googleTokenExpiry;
    }
}
