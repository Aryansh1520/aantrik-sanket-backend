package com.aantriksanket.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", nullable = false, columnDefinition = "jsonb")
    private Map<String, Map<String, Boolean>> features;

    // Fixed validity for special plans (Trial, Friends & Family) - if set, ignores tiered validity
    @Column(name = "fixed_validity_days")
    private Integer fixedValidityDays;

    // Validity (days) per tier - used only if fixedValidityDays is null
    @Column(name = "weekly_validity")
    private Integer weeklyValidity;

    @Column(name = "monthly_validity")
    private Integer monthlyValidity;

    @Column(name = "yearly_validity")
    private Integer yearlyValidity;

    @Column(name = "attributes", columnDefinition = "TEXT")
    private String attributes;

    // Prices per tier (INR)
    @Column(name = "weekly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal weeklyPrice;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "weekly_discount", nullable = false)
    private Integer weeklyDiscount; // percentage

    @Column(name = "monthly_discount", nullable = false)
    private Integer monthlyDiscount; // percentage

    @Column(name = "yearly_discount", nullable = false)
    private Integer yearlyDiscount; // percentage

    public SubscriptionPlan() {
    }

    public SubscriptionPlan(String name,
                          Map<String, Map<String, Boolean>> features,
                          Integer fixedValidityDays,
                          Integer weeklyValidity,
                          Integer monthlyValidity,
                          Integer yearlyValidity,
                          String attributes,
                          BigDecimal weeklyPrice,
                          BigDecimal monthlyPrice,
                          BigDecimal yearlyPrice,
                          Integer weeklyDiscount,
                          Integer monthlyDiscount,
                          Integer yearlyDiscount) {
        this.name = name;
        this.features = features;
        this.fixedValidityDays = fixedValidityDays;
        this.weeklyValidity = weeklyValidity;
        this.monthlyValidity = monthlyValidity;
        this.yearlyValidity = yearlyValidity;
        this.attributes = attributes;
        this.weeklyPrice = weeklyPrice;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.weeklyDiscount = weeklyDiscount;
        this.monthlyDiscount = monthlyDiscount;
        this.yearlyDiscount = yearlyDiscount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Map<String, Boolean>> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Map<String, Boolean>> features) {
        this.features = features;
    }

    public Integer getWeeklyValidity() {
        return weeklyValidity;
    }

    public void setWeeklyValidity(Integer weeklyValidity) {
        this.weeklyValidity = weeklyValidity;
    }

    public Integer getMonthlyValidity() {
        return monthlyValidity;
    }

    public void setMonthlyValidity(Integer monthlyValidity) {
        this.monthlyValidity = monthlyValidity;
    }

    public Integer getYearlyValidity() {
        return yearlyValidity;
    }

    public void setYearlyValidity(Integer yearlyValidity) {
        this.yearlyValidity = yearlyValidity;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public BigDecimal getWeeklyPrice() {
        return weeklyPrice;
    }

    public void setWeeklyPrice(BigDecimal weeklyPrice) {
        this.weeklyPrice = weeklyPrice;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(BigDecimal yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public Integer getWeeklyDiscount() {
        return weeklyDiscount;
    }

    public void setWeeklyDiscount(Integer weeklyDiscount) {
        this.weeklyDiscount = weeklyDiscount;
    }

    public Integer getMonthlyDiscount() {
        return monthlyDiscount;
    }

    public void setMonthlyDiscount(Integer monthlyDiscount) {
        this.monthlyDiscount = monthlyDiscount;
    }

    public Integer getYearlyDiscount() {
        return yearlyDiscount;
    }

    public void setYearlyDiscount(Integer yearlyDiscount) {
        this.yearlyDiscount = yearlyDiscount;
    }

    public Integer getFixedValidityDays() {
        return fixedValidityDays;
    }

    public void setFixedValidityDays(Integer fixedValidityDays) {
        this.fixedValidityDays = fixedValidityDays;
    }
}
