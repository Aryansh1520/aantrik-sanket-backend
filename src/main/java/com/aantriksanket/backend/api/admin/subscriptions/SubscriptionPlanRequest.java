package com.aantriksanket.backend.api.admin.subscriptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

public class SubscriptionPlanRequest {
    @Schema(description = "Plan name")
    private String name;

    @Schema(description = "Feature permissions by category, same shape as admin role permissions")
    private Map<String, Map<String, Boolean>> features;

    @Schema(description = "Attributes/notes for the plan")
    private String attributes;

    @Schema(description = "Weekly price (INR)")
    private BigDecimal weeklyPrice;

    @Schema(description = "Monthly price (INR)")
    private BigDecimal monthlyPrice;

    @Schema(description = "Yearly price (INR)")
    private BigDecimal yearlyPrice;

    @Schema(description = "Weekly validity in days (forced to 7 for normal plans)")
    private Integer weeklyValidity;

    @Schema(description = "Monthly validity in days (forced to 30 for normal plans)")
    private Integer monthlyValidity;

    @Schema(description = "Yearly validity in days (forced to 365 for normal plans)")
    private Integer yearlyValidity;

    @Schema(description = "Fixed validity in days (for Trial and Friends & Family plans only, overrides tiered validity)")
    private Integer fixedValidityDays;

    @Schema(description = "Weekly discount percentage")
    private Integer weeklyDiscount;

    @Schema(description = "Monthly discount percentage")
    private Integer monthlyDiscount;

    @Schema(description = "Yearly discount percentage")
    private Integer yearlyDiscount;

    public SubscriptionPlanRequest() {
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

    public Integer getFixedValidityDays() {
        return fixedValidityDays;
    }

    public void setFixedValidityDays(Integer fixedValidityDays) {
        this.fixedValidityDays = fixedValidityDays;
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
}
