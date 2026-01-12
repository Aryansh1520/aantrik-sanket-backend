package com.aantriksanket.backend.service.tenant.auth;

import com.aantriksanket.backend.api.tenant.auth.FirstLoginRequest;
import com.aantriksanket.backend.models.SubscriptionHistory;
import com.aantriksanket.backend.models.SubscriptionHistoryRepository;
import com.aantriksanket.backend.models.SubscriptionInterval;
import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.aantriksanket.backend.util.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TenantAuthService {

    private final TenantRepository tenantRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public TenantAuthService(TenantRepository tenantRepository,
                            SubscriptionHistoryRepository subscriptionHistoryRepository) {
        this.tenantRepository = tenantRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
    }

    @Transactional
    public Map<String, Object> completeFirstLogin(FirstLoginRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByEmailWithSubscription(request.getEmail());
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }

        Tenant tenant = tenantOpt.get();

        // Check if first login already completed
        if (tenant.getFirstLoggedIn()) {
            throw new RuntimeException("First login already completed — this action is permanently disabled.");
        }

        // Verify current password
        if (!PasswordHasher.matches(request.getCurrentPassword(), tenant.getPasswordHash())) {
            throw new RuntimeException("Invalid current password");
        }

        // Update password
        String newPasswordHash = PasswordHasher.hash(request.getNewPassword());
        tenant.setPasswordHash(newPasswordHash);
        tenant.setFirstLoggedIn(true);

        // Start trial subscription
        if (tenant.getSubscriptionPlan() != null) {
            OffsetDateTime now = OffsetDateTime.now();
            tenant.setSubscriptionStartAt(now);

            int validityDays = getValidityForSubscriptionType(tenant.getSubscriptionPlan(), tenant.getSubscriptionType());

            tenant.setSubscriptionValidity(validityDays);
            tenant.setSubscriptionEndAt(now.plusDays(validityDays));
            tenant.setSubscriptionDaysLeft(validityDays);

            // Log in subscription history
            SubscriptionHistory history = new SubscriptionHistory(
                    tenant,
                    tenant.getSubscriptionPlan(),
                    tenant.getSubscriptionType(),
                    tenant.getSubscriptionStartAt(),
                    tenant.getSubscriptionEndAt(),
                    validityDays
            );
            subscriptionHistoryRepository.save(history);
        }

        tenant = tenantRepository.save(tenant);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "First login completed successfully");
        data.put("subscriptionStarted", true);
        if (tenant.getSubscriptionStartAt() != null) {
            data.put("subscriptionStartAt", tenant.getSubscriptionStartAt());
            data.put("subscriptionEndAt", tenant.getSubscriptionEndAt());
            data.put("subscriptionDaysLeft", tenant.getSubscriptionDaysLeft());
        }

        return data;
    }

    private int getValidityForSubscriptionType(com.aantriksanket.backend.models.SubscriptionPlan plan, SubscriptionInterval type) {
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
}
