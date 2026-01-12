package com.aantriksanket.backend.config;

import com.aantriksanket.backend.models.Admin;
import com.aantriksanket.backend.models.AdminRepository;
import com.aantriksanket.backend.models.AdminRole;
import com.aantriksanket.backend.models.AdminRoleRepository;
import com.aantriksanket.backend.models.SubscriptionPlan;
import com.aantriksanket.backend.models.SubscriptionPlanRepository;
import com.aantriksanket.backend.util.security.PasswordHasher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataSeeder {

    private final AdminRepository adminRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public DataSeeder(AdminRepository adminRepository, AdminRoleRepository adminRoleRepository,
                     SubscriptionPlanRepository subscriptionPlanRepository) {
        this.adminRepository = adminRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        AdminRole superAdminRole = seedAdminRoles();
        seedSubscriptionPlans();
        seedDefaultAdmin(superAdminRole);
    }

    private AdminRole seedAdminRoles() {
        // Build the required permission categories
        Map<String, Map<String, Boolean>> requiredPermissions = new HashMap<>();

        Map<String, Boolean> authPermissions = new HashMap<>();
        authPermissions.put("create", true);
        authPermissions.put("read", true);
        authPermissions.put("update", true);
        authPermissions.put("delete", true);
        requiredPermissions.put("auth", authPermissions);

        Map<String, Boolean> rolesPermissions = new HashMap<>();
        rolesPermissions.put("create", true);
        rolesPermissions.put("read", true);
        rolesPermissions.put("update", true);
        rolesPermissions.put("delete", true);
        requiredPermissions.put("roles", rolesPermissions);

        Map<String, Boolean> subscriptionsPermissions = new HashMap<>();
        subscriptionsPermissions.put("create", true);
        subscriptionsPermissions.put("read", true);
        subscriptionsPermissions.put("update", true);
        subscriptionsPermissions.put("delete", true);
        requiredPermissions.put("subscriptions", subscriptionsPermissions);

        Map<String, Boolean> manageTenantsPermissions = new HashMap<>();
        manageTenantsPermissions.put("create", true);
        manageTenantsPermissions.put("read", true);
        manageTenantsPermissions.put("update", true);
        manageTenantsPermissions.put("delete", true);
        requiredPermissions.put("manage_tenants", manageTenantsPermissions);

        // Ensure Super Admin exists and has all required permissions
        AdminRole superAdminRole = adminRoleRepository.findByRoleName("Super Admin")
                .orElseGet(() -> {
                    AdminRole role = new AdminRole("Super Admin", new HashMap<>());
                    return adminRoleRepository.save(role);
                });

        Map<String, Map<String, Boolean>> currentPerms =
                superAdminRole.getRolePermissions() == null ? new HashMap<>() : superAdminRole.getRolePermissions();

        boolean updated = false;
        for (Map.Entry<String, Map<String, Boolean>> entry : requiredPermissions.entrySet()) {
            String category = entry.getKey();
            Map<String, Boolean> required = entry.getValue();
            Map<String, Boolean> existing = currentPerms.get(category);
            if (existing == null || existing.size() < required.size() || !existing.values().stream().allMatch(Boolean::booleanValue)) {
                currentPerms.put(category, required);
                updated = true;
            }
        }

        if (updated) {
            superAdminRole.setRolePermissions(currentPerms);
            superAdminRole = adminRoleRepository.save(superAdminRole);
        }

        return superAdminRole;
    }

    private void seedSubscriptionPlans() {
        // Check if subscription plans already exist
        if (subscriptionPlanRepository.count() > 0) {
            return;
        }

        // Create Trial subscription plan
        Map<String, Map<String, Boolean>> trialFeatures = new HashMap<>();
        // Trial has basic features - can be customized
        Map<String, Boolean> basicFeatures = new HashMap<>();
        basicFeatures.put("create", true);
        basicFeatures.put("read", true);
        basicFeatures.put("update", false);
        basicFeatures.put("delete", false);
        trialFeatures.put("basic", basicFeatures);

        SubscriptionPlan trialPlan = new SubscriptionPlan(
                "Trial",
                trialFeatures,
                30, // fixed validity days (admin can edit)
                null, // weekly validity (not used for fixed plans)
                null, // monthly validity (not used for fixed plans)
                null, // yearly validity (not used for fixed plans)
                "Trial subscription plan with limited features",
                BigDecimal.ZERO, // weekly price
                BigDecimal.ZERO, // monthly price
                BigDecimal.ZERO, // yearly price
                0, // No discounts for trial
                0,
                0
        );
        subscriptionPlanRepository.save(trialPlan);

        // Create Friends and Family Plan
        // 100 years = 36500 days (100 * 365)
        Map<String, Map<String, Boolean>> friendsFamilyFeatures = new HashMap<>();
        // Full features
        Map<String, Boolean> fullFeatures = new HashMap<>();
        fullFeatures.put("create", true);
        fullFeatures.put("read", true);
        fullFeatures.put("update", true);
        fullFeatures.put("delete", true);
        friendsFamilyFeatures.put("all", fullFeatures);

        SubscriptionPlan friendsFamilyPlan = new SubscriptionPlan(
                "Friends and Family Plan",
                friendsFamilyFeatures,
                36500, // fixed validity days (100 years)
                null, // weekly validity (not used for fixed plans)
                null, // monthly validity (not used for fixed plans)
                null, // yearly validity (not used for fixed plans)
                "Friends and Family Plan with full features and 100 years validity",
                BigDecimal.ZERO, // weekly price
                BigDecimal.ZERO, // monthly price
                BigDecimal.ZERO, // yearly price
                10, // 10% discount for weekly
                15, // 15% discount for monthly
                20  // 20% discount for yearly
        );
        subscriptionPlanRepository.save(friendsFamilyPlan);
    }

    private void seedDefaultAdmin(AdminRole superAdminRole) {
        if (superAdminRole == null) {
            return;
        }
        if (adminRepository.count() == 0) {
            String defaultEmail = "aryan152015@gmail.com";
            String defaultPassword = "Aryan@1520";
            String passwordHash = PasswordHasher.hash(defaultPassword);

            Admin defaultAdmin = new Admin(
                    defaultEmail,
                    passwordHash,
                    "Aryan",
                    superAdminRole
            );

            adminRepository.save(defaultAdmin);
        }
    }
}
