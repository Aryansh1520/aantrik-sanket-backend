package com.aantriksanket.backend.api.admin.tenants;

import com.aantriksanket.backend.models.SubscriptionInterval;

import java.util.UUID;

public class UpdateTenantRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String domainName;
    private UUID subscriptionPlanId;
    private SubscriptionInterval subscriptionType;
    private Boolean isActive;

    public UpdateTenantRequest() {
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
}
