package com.aantriksanket.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_history")
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false, length = 20)
    private SubscriptionInterval subscriptionType;

    @Column(name = "subscription_start_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime subscriptionStartAt;

    @Column(name = "subscription_end_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime subscriptionEndAt;

    @Column(name = "validity_days", nullable = false)
    private Integer validityDays;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public SubscriptionHistory() {
    }

    public SubscriptionHistory(Tenant tenant, SubscriptionPlan subscriptionPlan,
                              SubscriptionInterval subscriptionType,
                              OffsetDateTime subscriptionStartAt, OffsetDateTime subscriptionEndAt,
                              Integer validityDays) {
        this.tenant = tenant;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionType = subscriptionType;
        this.subscriptionStartAt = subscriptionStartAt;
        this.subscriptionEndAt = subscriptionEndAt;
        this.validityDays = validityDays;
    }

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

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public SubscriptionInterval getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionInterval subscriptionType) {
        this.subscriptionType = subscriptionType;
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

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
