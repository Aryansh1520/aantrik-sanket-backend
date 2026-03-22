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

    private static final String TRIAL_PLAN_NAME = "Trial";
    private static final String FF_PLAN_NAME = "Friends and Family Plan";
    private static final int FF_FIXED_VALIDITY_DAYS = 36500; // 100 years

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    private boolean isSpecialFixedPlan(String name) {
        return TRIAL_PLAN_NAME.equalsIgnoreCase(name) || FF_PLAN_NAME.equalsIgnoreCase(name);
    }

    private void enforceValidityForPlan(SubscriptionPlanRequest request) {
        if (isSpecialFixedPlan(request.getName())) {
            // For special plans, require fixedValidityDays and set tiered validities to null
            if (request.getFixedValidityDays() == null) {
                throw new RuntimeException("fixedValidityDays is required for Trial and Friends & Family plans");
            }
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

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getFeatures(),
                plan.getFixedValidityDays(),
                plan.getIsStatic(),
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
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        // Block creating plans with reserved static plan names
        if (isSpecialFixedPlan(request.getName())) {
            throw new RuntimeException("Cannot create a plan with reserved name '" + request.getName() + "'. Trial and Friends & Family plans are system-managed.");
        }

        if (subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subscription plan name already exists");
        }

        ensurePricesProvided(request);
        enforceValidityForPlan(request);

        SubscriptionPlan plan = new SubscriptionPlan(
                request.getName(),
                request.getFeatures(),
                request.getFixedValidityDays(),
                false, // not a static plan
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
        return toResponse(plan);
    }

    public List<SubscriptionPlanResponse> getAll() {
        return subscriptionPlanRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SubscriptionPlanResponse getById(UUID id) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }
        return toResponse(planOpt.get());
    }

    @Transactional
    public SubscriptionPlanResponse update(UUID id, SubscriptionPlanRequest request) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }

        SubscriptionPlan plan = planOpt.get();

        // --- Static plan protections ---
        if (Boolean.TRUE.equals(plan.getIsStatic())) {
            // Block renaming static plans
            if (!plan.getName().equalsIgnoreCase(request.getName())) {
                throw new RuntimeException("Cannot rename a static plan ('" + plan.getName() + "')");
            }

            // Friends & Family: validity is immutable (always 36500)
            if (FF_PLAN_NAME.equalsIgnoreCase(plan.getName())) {
                request.setFixedValidityDays(FF_FIXED_VALIDITY_DAYS);
            }
            // Trial: admin CAN change fixedValidityDays — no override needed
        }

        // Check if name is being changed and if new name already exists
        if (!plan.getName().equals(request.getName()) &&
            subscriptionPlanRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subscription plan name already exists");
        }

        // Block non-static plans from using reserved names
        if (!Boolean.TRUE.equals(plan.getIsStatic()) && isSpecialFixedPlan(request.getName())) {
            throw new RuntimeException("Cannot rename a plan to reserved name '" + request.getName() + "'");
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
        return toResponse(plan);
    }

    @Transactional
    public Map<String, Object> delete(UUID id) {
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Subscription plan not found");
        }

        SubscriptionPlan plan = planOpt.get();

        // Block deletion of static plans
        if (Boolean.TRUE.equals(plan.getIsStatic())) {
            throw new RuntimeException("Cannot delete static plan '" + plan.getName() + "'. Trial and Friends & Family plans are system-managed.");
        }

        // TODO: Check if any tenant is using this plan before deleting
        subscriptionPlanRepository.deleteById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Subscription plan deleted successfully");
        return data;
    }
}
