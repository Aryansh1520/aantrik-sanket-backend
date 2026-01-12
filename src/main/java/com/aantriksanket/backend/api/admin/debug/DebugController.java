package com.aantriksanket.backend.api.admin.debug;

import com.aantriksanket.backend.models.Admin;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.PermissionChecker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/debug")
@Tag(name = "Debug")
@SecurityRequirement(name = "bearerAuth")
public class DebugController {

    private final PermissionChecker permissionChecker;

    public DebugController(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @GetMapping("/auth-info")
    @Operation(summary = "Get current authentication info (debug endpoint)")
    public ResponseEntity<ApiResponse> getAuthInfo() {
        Map<String, Object> data = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        data.put("authenticated", authentication != null && authentication.isAuthenticated());
        data.put("principalType", authentication != null && authentication.getPrincipal() != null
                ? authentication.getPrincipal().getClass().getName()
                : "null");

        if (authentication != null && authentication.getPrincipal() instanceof Admin) {
            Admin admin = (Admin) authentication.getPrincipal();
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId().toString());
            adminInfo.put("email", admin.getEmail());
            adminInfo.put("fullName", admin.getFullName());
            adminInfo.put("isActive", admin.getIsActive());

            if (admin.getRole() != null) {
                Map<String, Object> roleInfo = new HashMap<>();
                roleInfo.put("id", admin.getRole().getId().toString());
                roleInfo.put("roleName", admin.getRole().getRoleName());
                roleInfo.put("permissions", admin.getRole().getRolePermissions());
                adminInfo.put("role", roleInfo);
            } else {
                adminInfo.put("role", "NULL");
            }

            data.put("admin", adminInfo);
        }

        // Test permission check
        Map<String, Object> permissionTests = new HashMap<>();
        permissionTests.put("roles_read", permissionChecker.hasPermission("roles", "GET"));
        permissionTests.put("roles_create", permissionChecker.hasPermission("roles", "POST"));
        permissionTests.put("auth_read", permissionChecker.hasPermission("auth", "GET"));
        permissionTests.put("auth_create", permissionChecker.hasPermission("auth", "POST"));
        data.put("permissionTests", permissionTests);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
