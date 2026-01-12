package com.aantriksanket.backend.util.security;

import com.aantriksanket.backend.models.Admin;
import com.aantriksanket.backend.models.AdminRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PermissionChecker {

    private static final Logger logger = LoggerFactory.getLogger(PermissionChecker.class);

    public enum UserType {
        ADMIN,
        TENANT
    }

    public boolean hasPermission(String apiCategory, String httpMethod) {
        logger.debug("PermissionChecker: Checking permission for category={}, method={}", apiCategory, httpMethod);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.warn("PermissionChecker: Authentication is null");
            return false;
        }

        if (!authentication.isAuthenticated()) {
            logger.warn("PermissionChecker: Authentication is not authenticated");
            return false;
        }

        Object principal = authentication.getPrincipal();
        logger.debug("PermissionChecker: Principal type={}", principal != null ? principal.getClass().getName() : "null");

        if (principal instanceof Admin) {
            Admin admin = (Admin) principal;
            logger.debug("PermissionChecker: Admin found, id={}, email={}", admin.getId(), admin.getEmail());
            return checkAdminPermission(admin, apiCategory, httpMethod);
        }

        logger.warn("PermissionChecker: Principal is not an Admin instance");
        // TODO: Implement tenant permission checking based on subscription
        // For now, tenants don't have permissions
        return false;
    }

    private boolean checkAdminPermission(Admin admin, String apiCategory, String httpMethod) {
        logger.debug("PermissionChecker: Checking admin permission for adminId={}, category={}, method={}",
                admin.getId(), apiCategory, httpMethod);

        AdminRole role = admin.getRole();
        if (role == null) {
            logger.error("PermissionChecker: Admin role is null for adminId={}", admin.getId());
            return false;
        }

        logger.debug("PermissionChecker: Admin role found, roleId={}, roleName={}", role.getId(), role.getRoleName());

        Map<String, Map<String, Boolean>> permissions = role.getRolePermissions();
        if (permissions == null) {
            logger.error("PermissionChecker: Role permissions map is null for roleId={}, roleName={}",
                    role.getId(), role.getRoleName());
            return false;
        }

        logger.debug("PermissionChecker: Role permissions map size={}, keys={}",
                permissions.size(), permissions.keySet());

        Map<String, Boolean> categoryPermissions = permissions.get(apiCategory);
        if (categoryPermissions == null) {
            logger.error("PermissionChecker: Category '{}' not found in permissions. Available categories: {}",
                    apiCategory, permissions.keySet());
            return false;
        }

        logger.debug("PermissionChecker: Category permissions found for '{}', permissions={}",
                apiCategory, categoryPermissions);

        // Map HTTP methods to permission keys
        String permissionKey = mapHttpMethodToPermission(httpMethod);
        logger.debug("PermissionChecker: Mapped HTTP method '{}' to permission key '{}'", httpMethod, permissionKey);

        Boolean hasPermission = categoryPermissions.get(permissionKey);
        logger.debug("PermissionChecker: Permission value for key '{}' = {}", permissionKey, hasPermission);

        boolean result = Boolean.TRUE.equals(hasPermission);
        logger.debug("PermissionChecker: Final permission result={} for category={}, method={}",
                result, apiCategory, httpMethod);

        return result;
    }

    private String mapHttpMethodToPermission(String httpMethod) {
        return switch (httpMethod.toUpperCase()) {
            case "GET" -> "read";
            case "POST" -> "create";
            case "PUT", "PATCH" -> "update";
            case "DELETE" -> "delete";
            default -> "read"; // Default to read for unknown methods
        };
    }

    public UserType getUserType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Admin) {
            return UserType.ADMIN;
        }

        // TODO: Check for tenant
        return null;
    }

    public Admin getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Admin) {
            return (Admin) authentication.getPrincipal();
        }
        return null;
    }
}
