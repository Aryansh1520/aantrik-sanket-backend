package com.aantriksanket.backend.api.admin.auth;

import com.aantriksanket.backend.service.admin.auth.AdminAuthService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login as admin")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = adminAuthService.login(request);
            Map<String, Object> data = new HashMap<>();
            data.put("admin", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new admin")
    @SecurityRequirement(name = "bearerAuth")
    @RequiresPermission(category = "auth", method = "create")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> data = adminAuthService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset admin password (public endpoint)")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            Map<String, Object> data = adminAuthService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }
}
