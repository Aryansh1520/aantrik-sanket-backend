package com.aantriksanket.backend.api.tenant.auth;

import com.aantriksanket.backend.service.tenant.auth.TenantAuthService;
import com.aantriksanket.backend.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/auth")
@Tag(name = "Tenant Auth")
public class TenantAuthController {

    private final TenantAuthService tenantAuthService;

    public TenantAuthController(TenantAuthService tenantAuthService) {
        this.tenantAuthService = tenantAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "Tenant login — returns JWT or requiresFirstLogin flag")
    public ResponseEntity<ApiResponse> login(@RequestBody TenantLoginRequest request) {
        try {
            Map<String, Object> data = tenantAuthService.login(request);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("deactivated")
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PostMapping("/first-login-complete")
    @Operation(summary = "Complete first login: change password and start trial subscription")
    public ResponseEntity<ApiResponse> completeFirstLogin(@RequestBody FirstLoginRequest request) {
        try {
            Map<String, Object> data = tenantAuthService.completeFirstLogin(request);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("already completed")
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password using old password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Map<String, Object> data = tenantAuthService.changePassword(request);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Reset password — generates and returns a new password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            Map<String, Object> data = tenantAuthService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }
}
