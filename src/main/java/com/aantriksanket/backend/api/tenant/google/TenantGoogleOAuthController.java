package com.aantriksanket.backend.api.tenant.google;

import com.aantriksanket.backend.service.tenant.google.TenantGoogleOAuthService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.TenantJwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenant/google/oauth")
@Tag(name = "Tenant Google OAuth")
public class TenantGoogleOAuthController {

    private final TenantGoogleOAuthService googleOAuthService;
    private final TenantJwtUtil tenantJwtUtil;

    public TenantGoogleOAuthController(TenantGoogleOAuthService googleOAuthService,
                                        TenantJwtUtil tenantJwtUtil) {
        this.googleOAuthService = googleOAuthService;
        this.tenantJwtUtil = tenantJwtUtil;
    }

    // ──────────────────────────────────────────────
    // 1. INITIATE OAuth — requires tenant JWT
    // ──────────────────────────────────────────────
    @GetMapping("/initiate")
    @Operation(summary = "Initiate Google OAuth flow — returns the Google authorization URL")
    public ResponseEntity<ApiResponse> initiate(HttpServletRequest request) {
        try {
            UUID tenantId = extractTenantId(request);
            String authUrl = googleOAuthService.buildAuthorizationUrl(tenantId);
            Map<String, Object> data = new HashMap<>();
            data.put("auth_url", authUrl);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 2. CALLBACK — public (Google redirects here)
    // ──────────────────────────────────────────────
    @GetMapping("/callback")
    @Operation(summary = "Google OAuth callback — exchanges code for tokens and saves them")
    public ResponseEntity<String> callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state) {
        try {
            googleOAuthService.handleCallback(code, state);
        } catch (RuntimeException e) {
            // Return an error page so the popup can relay the failure
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("<html><body><script>" +
                          "window.opener && window.opener.postMessage({ type: 'GOOGLE_ERROR', error: '"
                          + e.getMessage().replace("'", "\\'") + "' }, '*');" +
                          "window.close();" +
                          "</script><p>Error: " + e.getMessage() + "</p></body></html>");
        }

        return ResponseEntity.ok(
                "<html><body>" +
                "<script>" +
                "window.opener && window.opener.postMessage({ type: 'GOOGLE_CONNECTED' }, '*');" +
                "window.close();" +
                "</script>" +
                "<p>Google connected successfully. You can close this window.</p>" +
                "</body></html>"
        );
    }

    // ──────────────────────────────────────────────
    // 3. STATUS — requires tenant JWT
    // ──────────────────────────────────────────────
    @GetMapping("/status")
    @Operation(summary = "Get Google OAuth connection status for the current tenant")
    public ResponseEntity<ApiResponse> status(HttpServletRequest request) {
        try {
            UUID tenantId = extractTenantId(request);
            Map<String, Object> data = googleOAuthService.getStatus(tenantId);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 4. DISCONNECT — requires tenant JWT
    // ──────────────────────────────────────────────
    @PostMapping("/disconnect")
    @Operation(summary = "Disconnect Google account — clears all stored OAuth tokens")
    public ResponseEntity<ApiResponse> disconnect(HttpServletRequest request) {
        try {
            UUID tenantId = extractTenantId(request);
            googleOAuthService.disconnect(tenantId);
            return ResponseEntity.ok(ApiResponse.success("message", "Google account disconnected successfully."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // Helper: extract and validate tenant JWT
    // ──────────────────────────────────────────────
    private UUID extractTenantId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        try {
            String email = tenantJwtUtil.getEmailFromToken(token);
            if (!tenantJwtUtil.validateToken(token, email)) {
                throw new UnauthorizedException("Token is invalid or expired");
            }
            return tenantJwtUtil.getTenantIdFromToken(token);
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid tenant token: " + e.getMessage());
        }
    }

    private static class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String message) {
            super(message);
        }
    }
}
