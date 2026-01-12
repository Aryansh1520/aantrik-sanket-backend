package com.aantriksanket.backend.service.admin.tenants;

import com.aantriksanket.backend.api.admin.tenants.CreateTenantRequest;
import com.aantriksanket.backend.api.admin.tenants.TenantResponse;
import com.aantriksanket.backend.api.admin.tenants.UpdateTenantRequest;
import com.aantriksanket.backend.models.*;
import com.aantriksanket.backend.util.PasswordGenerator;
import com.aantriksanket.backend.util.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public TenantService(TenantRepository tenantRepository,
                        SubscriptionPlanRepository subscriptionPlanRepository,
                        SubscriptionHistoryRepository subscriptionHistoryRepository) {
        this.tenantRepository = tenantRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
    }

    @Transactional
    public Map<String, Object> create(CreateTenantRequest request) {
        // Check for duplicates
        if (tenantRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
        if (tenantRepository.existsByDomainName(request.getDomainName())) {
            throw new RuntimeException("Domain name already exists");
        }

        // Generate email from domain name
        String email = generateEmailFromDomain(request.getDomainName());
        if (tenantRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Generate password
        String generatedPassword = PasswordGenerator.generate(12);
        String passwordHash = PasswordHasher.hash(generatedPassword);

        // Get trial plan (default)
        SubscriptionPlan trialPlan = subscriptionPlanRepository.findByName("Trial")
                .orElseThrow(() -> new RuntimeException("Trial subscription plan not found"));

        // Create tenant
        Tenant tenant = new Tenant(
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                email,
                passwordHash,
                request.getDomainName(),
                trialPlan
        );

        tenant.setSubscriptionType(SubscriptionInterval.MONTHLY);
        tenant.setSubscriptionValidity(getValidityForSubscriptionType(trialPlan, SubscriptionInterval.MONTHLY));
        // Trial doesn't start until first login, so dates are null
        tenant.setSubscriptionStartAt(null);
        tenant.setSubscriptionEndAt(null);
        tenant.setSubscriptionDaysLeft(0);

        tenant = tenantRepository.save(tenant);

        // Build response
        Map<String, Object> data = new HashMap<>();
        data.put("tenant", toResponse(tenant));
        data.put("generatedPassword", generatedPassword);
        data.put("email", email);

        return data;
    }

    public List<TenantResponse> getAll() {
        return tenantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TenantResponse getById(UUID id) {
        Optional<Tenant> tenantOpt = tenantRepository.findByIdWithSubscription(id);
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }
        return toResponse(tenantOpt.get());
    }

    @Transactional
    public TenantResponse update(UUID id, UpdateTenantRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByIdWithSubscription(id);
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }

        Tenant tenant = tenantOpt.get();

        // Update basic fields
        if (request.getFirstName() != null) {
            tenant.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            tenant.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(tenant.getPhoneNumber())) {
            if (tenantRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new RuntimeException("Phone number already exists");
            }
            tenant.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDomainName() != null && !request.getDomainName().equals(tenant.getDomainName())) {
            if (tenantRepository.existsByDomainName(request.getDomainName())) {
                throw new RuntimeException("Domain name already exists");
            }
            tenant.setDomainName(request.getDomainName());
        }
        if (request.getIsActive() != null) {
            tenant.setIsActive(request.getIsActive());
        }

        // Handle subscription change
        boolean subscriptionChanged = false;
        SubscriptionPlan oldPlan = tenant.getSubscriptionPlan();
        SubscriptionInterval oldType = tenant.getSubscriptionType();

        if (request.getSubscriptionPlanId() != null) {
            Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findById(request.getSubscriptionPlanId());
            if (planOpt.isEmpty()) {
                throw new RuntimeException("Subscription plan not found");
            }
            SubscriptionPlan newPlan = planOpt.get();
            if (oldPlan == null || !oldPlan.getId().equals(newPlan.getId())) {
                subscriptionChanged = true;
                tenant.setSubscriptionPlan(newPlan);
            }
        }

        if (request.getSubscriptionType() != null &&
            (oldType == null || !oldType.equals(request.getSubscriptionType()))) {
            subscriptionChanged = true;
            tenant.setSubscriptionType(request.getSubscriptionType());
        }

        // If subscription changed and tenant has logged in, update subscription dates
        if (subscriptionChanged && tenant.getFirstLoggedIn()) {
            updateSubscriptionDates(tenant);

            // Log in history
            SubscriptionHistory history = new SubscriptionHistory(
                    tenant,
                    tenant.getSubscriptionPlan(),
                    tenant.getSubscriptionType(),
                    tenant.getSubscriptionStartAt(),
                    tenant.getSubscriptionEndAt(),
                    tenant.getSubscriptionValidity()
            );
            subscriptionHistoryRepository.save(history);
        }

        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    @Transactional
    public Map<String, Object> delete(UUID id) {
        Optional<Tenant> tenantOpt = tenantRepository.findById(id);
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }

        tenantRepository.deleteById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Tenant deleted successfully");
        return data;
    }

    private String generateEmailFromDomain(String domainName) {
        // Generate email like: admin@domainname.com
        return "admin@" + domainName;
    }

    private void updateSubscriptionDates(Tenant tenant) {
        if (tenant.getSubscriptionPlan() == null) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        tenant.setSubscriptionStartAt(now);

        int validityDays = getValidityForSubscriptionType(tenant.getSubscriptionPlan(), tenant.getSubscriptionType());

        tenant.setSubscriptionValidity(validityDays);
        tenant.setSubscriptionEndAt(now.plusDays(validityDays));
        tenant.setSubscriptionDaysLeft(validityDays);
    }

    private int getValidityForSubscriptionType(SubscriptionPlan plan, SubscriptionInterval type) {
        // If plan has fixed validity (Trial, Friends & Family), use that regardless of subscription type
        if (plan.getFixedValidityDays() != null) {
            return plan.getFixedValidityDays();
        }
        // Otherwise use tiered validity based on subscription type
        return switch (type) {
            case WEEKLY -> plan.getWeeklyValidity() != null ? plan.getWeeklyValidity() : 7;
            case MONTHLY -> plan.getMonthlyValidity() != null ? plan.getMonthlyValidity() : 30;
            case YEARLY -> plan.getYearlyValidity() != null ? plan.getYearlyValidity() : 365;
        };
    }

    private TenantResponse toResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setFirstName(tenant.getFirstName());
        response.setLastName(tenant.getLastName());
        response.setPhoneNumber(tenant.getPhoneNumber());
        response.setEmail(tenant.getEmail());
        response.setDomainName(tenant.getDomainName());
        if (tenant.getSubscriptionPlan() != null) {
            response.setSubscriptionPlanId(tenant.getSubscriptionPlan().getId());
            response.setSubscriptionPlanName(tenant.getSubscriptionPlan().getName());
        }
        response.setSubscriptionValidity(tenant.getSubscriptionValidity());
        response.setSubscriptionType(tenant.getSubscriptionType());
        response.setIsActive(tenant.getIsActive());
        response.setFirstLoggedIn(tenant.getFirstLoggedIn());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setUpdatedAt(tenant.getUpdatedAt());
        response.setSubscriptionStartAt(tenant.getSubscriptionStartAt());
        response.setSubscriptionEndAt(tenant.getSubscriptionEndAt());
        response.setSubscriptionDaysLeft(tenant.getSubscriptionDaysLeft());
        return response;
    }
}
