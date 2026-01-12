package com.aantriksanket.backend.api.admin.subscriptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private Map<String, Map<String, Boolean>> features;
    private String attributes;

    // Fixed validity for special plans
    private Integer fixedValidityDays;

    // Validity per tier
    private Integer weeklyValidity;
    private Integer monthlyValidity;
    private Integer yearlyValidity;

    // Prices per tier
    private BigDecimal weeklyPrice;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;

    private Integer weeklyDiscount;
    private Integer monthlyDiscount;
    private Integer yearlyDiscount;

    public SubscriptionPlanResponse() {
    }

    public SubscriptionPlanResponse(UUID id,
                                   String name,
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
        this.id = id;
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
