package com.aantriksanket.backend.service.admin.subscriptions;

import com.aantriksanket.backend.api.admin.subscriptions.SubscriptionPlanRequest;
import com.aantriksanket.backend.api.admin.subscriptions.SubscriptionPlanResponse;
import com.aantriksanket.backend.models.SubscriptionPlan;
import com.aantriksanket.backend.models.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    private boolean isSpecialFixedPlan(String name) {
        return "Trial".equalsIgnoreCase(name) || "Friends and Family Plan".equalsIgnoreCase(name);
    }

    private void enforceValidityForPlan(SubscriptionPlanRequest request) {
        if (isSpecialFixedPlan(request.getName())) {
            if ("Trial".equalsIgnoreCase(request.getName())) {
                // Trial: fixed validity; default 14 if not provided (admin can change)
                if (request.getFixedValidityDays() == null) {
                    request.setFixedValidityDays(14);
                }
            } else {
                // Friends and Family: fixed 100 years; not editable
                request.setFixedValidityDays(36500);
                // Force free pricing and zero discounts
                request.setWeeklyPrice(java.math.BigDecimal.ZERO);
                request.setMonthlyPrice(java.math.BigDecimal.ZERO);
                request.setYearlyPrice(java.math.BigDecimal.ZERO);
                request.setWeeklyDiscount(0);
                request.setMonthlyDiscount(0);
                request.setYearlyDiscount(0);
            }
            // Special plans do not use tiered validity
            request.setWeeklyValidity(null);
            request.setMonthlyValidity(null);
            request.setYearlyValidity(null);
        } else {
            // For normal plans, require tiered validities and set fixedValidityDays to null
            if (request.getWeeklyValidity() == null || request.getMonthlyValidity() == null || request.getYearlyValidity() == null) {
                throw new RuntimeException("weeklyValidity, monthlyValidity, and yearlyValidity are required for normal plans");
            }
            // Force standard validity for normal plans
            request.setWeeklyValidity(7);
            request.setMonthlyValidity(30);
            request.setYearlyValidity(365);
            request.setFixedValidityDays(null);
        }
    }

    private void ensurePricesProvided(SubscriptionPlanRequest request) {
        if (request.getWeeklyPrice() == null || request.getMonthlyPrice() == null || request.getYearlyPrice() == null) {
            throw new RuntimeException("weeklyPrice, monthlyPrice, and yearlyPrice are required");
        }
    }

    @Transactional
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        if (subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subscription plan name already exists");
        }

        ensurePricesProvided(request);
        enforceValidityForPlan(request);

        SubscriptionPlan plan = new SubscriptionPlan(
                request.getName(),
                request.getFeatures(),
                request.getFixedValidityDays(),
                request.getWeeklyValidity(),
                request.getMonthlyValidity(),
                request.getYearlyValidity(),
                request.getAttributes(),
                request.getWeeklyPrice(),
                request.getMonthlyPrice(),
                request.getYearlyPrice(),
                request.getWeeklyDiscount(),
                request.getMonthlyDiscount(),
                request.getYearlyDiscount()
        );

        plan = subscriptionPlanRepository.save(plan);

        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getFeatures(),
                plan.getFixedValidityDays(),
                plan.getWeeklyValidity(),
                plan.getMonthlyValidity(),
                plan.getYearlyValidity(),
                plan.getAttributes(),
                plan.getWeeklyPrice(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getWeeklyDiscount(),
                plan.getMonthlyDiscount(),
                plan.getYearlyDiscount()
        );
    }

    public List<SubscriptionPlanResponse> getAll() {
        return subscriptionPlanRepository.findAll().stream()
                .map(plan -> new SubscriptionPlanResponse(
                        plan.getId(),
                        plan.getName(),
                        plan.getFeatures(),
                        plan.getFixedValidityDays(),
                        plan.getWeeklyValidity(),
                        plan.getMonthlyValidity(),
                        plan.getYearlyValidity(),
                        plan.getAttributes(),
                        plan.getWeeklyPrice(),
                        plan.getMonthlyPrice(),
                        plan.getYearlyPrice(),
                        plan.getWeeklyDiscount(),
                        plan.getMonthlyDiscount(),
                        plan.getYearlyDiscount()
                ))
                .collect(Collectors.toList());
    }

    public SubscriptionPlanResponse getById(UUID id) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }

        SubscriptionPlan plan = planOpt.get();
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getFeatures(),
                plan.getFixedValidityDays(),
                plan.getWeeklyValidity(),
                plan.getMonthlyValidity(),
                plan.getYearlyValidity(),
                plan.getAttributes(),
                plan.getWeeklyPrice(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getWeeklyDiscount(),
                plan.getMonthlyDiscount(),
                plan.getYearlyDiscount()
        );
    }

    @Transactional
    public SubscriptionPlanResponse update(UUID id, SubscriptionPlanRequest request) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }

        SubscriptionPlan plan = planOpt.get();

        // Check if name is being changed and if new name already exists
        if (!plan.getName().equals(request.getName()) &&
            subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subscription plan name already exists");
        }

        ensurePricesProvided(request);
        enforceValidityForPlan(request);

        plan.setName(request.getName());
        plan.setFeatures(request.getFeatures());
        plan.setFixedValidityDays(request.getFixedValidityDays());
        plan.setWeeklyValidity(request.getWeeklyValidity());
        plan.setMonthlyValidity(request.getMonthlyValidity());
        plan.setYearlyValidity(request.getYearlyValidity());
        plan.setAttributes(request.getAttributes());
        plan.setWeeklyPrice(request.getWeeklyPrice());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setWeeklyDiscount(request.getWeeklyDiscount());
        plan.setMonthlyDiscount(request.getMonthlyDiscount());
        plan.setYearlyDiscount(request.getYearlyDiscount());

        plan = subscriptionPlanRepository.save(plan);

        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getFeatures(),
                plan.getFixedValidityDays(),
                plan.getWeeklyValidity(),
                plan.getMonthlyValidity(),
                plan.getYearlyValidity(),
                plan.getAttributes(),
                plan.getWeeklyPrice(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getWeeklyDiscount(),
                plan.getMonthlyDiscount(),
                plan.getYearlyDiscount()
        );
    }

    @Transactional
    public Map<String, Object> delete(UUID id) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }

        // TODO: Check if any tenant is using this plan before deleting
        subscriptionPlanRepository.deleteById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Subscription plan deleted successfully");
        return data;
    }
}
