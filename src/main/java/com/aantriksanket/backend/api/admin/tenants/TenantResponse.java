package com.aantriksanket.backend.api.admin.tenants;

import com.aantriksanket.backend.models.SubscriptionInterval;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TenantResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String domainName;
    private UUID subscriptionPlanId;
    private String subscriptionPlanName;
    private Integer subscriptionValidity;
    private SubscriptionInterval subscriptionType;
    private Boolean isActive;
    private Boolean firstLoggedIn;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime subscriptionStartAt;
    private OffsetDateTime subscriptionEndAt;
    private Integer subscriptionDaysLeft;

    public TenantResponse() {
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

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public UUID getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(UUID subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public String getSubscriptionPlanName() {
        return subscriptionPlanName;
    }

    public void setSubscriptionPlanName(String subscriptionPlanName) {
        this.subscriptionPlanName = subscriptionPlanName;
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
}
