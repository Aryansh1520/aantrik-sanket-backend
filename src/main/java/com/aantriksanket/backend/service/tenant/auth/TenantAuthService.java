package com.aantriksanket.backend.service.tenant.auth;

import com.aantriksanket.backend.api.tenant.auth.ChangePasswordRequest;
import com.aantriksanket.backend.api.tenant.auth.FirstLoginRequest;
import com.aantriksanket.backend.api.tenant.auth.ForgotPasswordRequest;
import com.aantriksanket.backend.api.tenant.auth.TenantLoginRequest;
import com.aantriksanket.backend.models.SubscriptionHistory;
import com.aantriksanket.backend.models.SubscriptionHistoryRepository;
import com.aantriksanket.backend.models.SubscriptionInterval;
import com.aantriksanket.backend.models.SubscriptionPlan;
import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.aantriksanket.backend.util.PasswordGenerator;
import com.aantriksanket.backend.util.security.PasswordHasher;
import com.aantriksanket.backend.util.security.TenantJwtUtil;
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
    private final TenantJwtUtil tenantJwtUtil;

    public TenantAuthService(TenantRepository tenantRepository,
                            SubscriptionHistoryRepository subscriptionHistoryRepository,
                            TenantJwtUtil tenantJwtUtil) {
        this.tenantRepository = tenantRepository;
        this.subscriptionHistoryRepository = subscriptionHistoryRepository;
        this.tenantJwtUtil = tenantJwtUtil;
    }

    /**
     * Login: verify credentials. If firstLoggedIn is false, return requiresFirstLogin flag.
     * Otherwise issue a JWT token.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> login(TenantLoginRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByEmailWithSubscription(request.getEmail());
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Tenant tenant = tenantOpt.get();

        // Check if tenant is active
        if (!Boolean.TRUE.equals(tenant.getIsActive())) {
            throw new RuntimeException("Account is deactivated. Please contact support.");
        }

        // Verify password
        if (!PasswordHasher.matches(request.getPassword(), tenant.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        Map<String, Object> data = new HashMap<>();

        // If first login not completed, prompt them to complete it
        if (!tenant.getFirstLoggedIn()) {
            data.put("requiresFirstLogin", true);
            data.put("message", "Please complete first login to change your password and activate your trial.");
            return data;
        }

        // Issue JWT
        String token = tenantJwtUtil.generateToken(tenant.getId(), tenant.getEmail());
        data.put("token", token);
        data.put("tenantId", tenant.getId());
        data.put("email", tenant.getEmail());
        data.put("firstName", tenant.getFirstName());
        data.put("lastName", tenant.getLastName());
        data.put("domainName", tenant.getDomainName());
        data.put("requiresFirstLogin", false);

        return data;
    }

    /**
     * Complete first login: verify current password, set new password,
     * mark firstLoggedIn=true, and start the trial subscription.
     */
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

        // Issue JWT so they're logged in after first-login
        String token = tenantJwtUtil.generateToken(tenant.getId(), tenant.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("message", "First login completed successfully");
        data.put("token", token);
        data.put("subscriptionStarted", true);
        if (tenant.getSubscriptionStartAt() != null) {
            data.put("subscriptionStartAt", tenant.getSubscriptionStartAt());
            data.put("subscriptionEndAt", tenant.getSubscriptionEndAt());
            data.put("subscriptionDaysLeft", tenant.getSubscriptionDaysLeft());
        }

        return data;
    }

    /**
     * Change password: verify old password, set new password.
     */
    @Transactional
    public Map<String, Object> changePassword(ChangePasswordRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByEmail(request.getEmail());
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }

        Tenant tenant = tenantOpt.get();

        // Verify old password
        if (!PasswordHasher.matches(request.getOldPassword(), tenant.getPasswordHash())) {
            throw new RuntimeException("Invalid old password");
        }

        // Update password
        String newPasswordHash = PasswordHasher.hash(request.getNewPassword());
        tenant.setPasswordHash(newPasswordHash);
        tenantRepository.save(tenant);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Password changed successfully");
        return data;
    }

    /**
     * Forgot password: generate a new password and return it.
     * In production, this would send an email. For now, returns the new password in the response.
     */
    @Transactional
    public Map<String, Object> forgotPassword(ForgotPasswordRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findByEmail(request.getEmail());
        if (tenantOpt.isEmpty()) {
            throw new RuntimeException("Tenant not found");
        }

        Tenant tenant = tenantOpt.get();

        // Generate new password
        String newPassword = PasswordGenerator.generate(12);
        String newPasswordHash = PasswordHasher.hash(newPassword);
        tenant.setPasswordHash(newPasswordHash);
        tenantRepository.save(tenant);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Password has been reset");
        data.put("newPassword", newPassword);
        data.put("email", tenant.getEmail());
        return data;
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
            case FIXED -> plan.getFixedValidityDays() != null ? plan.getFixedValidityDays() : 0;
        };
    }
}
