package com.aantriksanket.backend.api.tenant.sessions;

import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.aantriksanket.backend.service.sessions.SessionService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.TenantJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenant/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final TenantJwtUtil tenantJwtUtil;
    private final TenantRepository tenantRepository;

    public SessionController(SessionService sessionService, TenantJwtUtil tenantJwtUtil, TenantRepository tenantRepository) {
        this.sessionService = sessionService;
        this.tenantJwtUtil = tenantJwtUtil;
        this.tenantRepository = tenantRepository;
    }

    private static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) { super(message); }
    }

    private Tenant getAuthenticatedTenant(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        try {
            UUID tenantId = tenantJwtUtil.getTenantIdFromToken(token);
            return tenantRepository.findById(tenantId).orElseThrow(() -> new UnauthorizedException("Tenant not found"));
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createSession(
            HttpServletRequest request,
            @RequestBody CreateSessionRequest payload) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            List<SessionResponse> responses = sessionService.createSession(therapist, payload);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", responses)));
        } catch (SessionService.ConflictException e) {
            return ResponseEntity.status(409).body(ApiResponse.failure("CONFLICT", e.getMessage()));
        } catch (SessionService.ValidationException e) {
            return ResponseEntity.status(422).body(ApiResponse.failure("VALIDATION_ERROR", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getSessions(
            HttpServletRequest request,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeCanceled) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            List<SessionResponse> responses = sessionService.getSessions(therapist, clientId, includeCanceled);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", responses)));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> getSessionById(
            HttpServletRequest request,
            @PathVariable UUID sessionId) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            SessionResponse response = sessionService.getSessionById(therapist, sessionId);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", response)));
        } catch (SessionService.ValidationException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure("NOT_FOUND", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<ApiResponse> getSeriesSessions(
            HttpServletRequest request,
            @PathVariable UUID seriesId) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            List<SessionResponse> responses = sessionService.getSeriesSessions(therapist, seriesId);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", responses)));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> patchSession(
            HttpServletRequest request,
            @PathVariable UUID sessionId,
            @RequestParam(required = false, defaultValue = "single") String scope,
            @RequestBody UpdateSessionRequest payload) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            List<SessionResponse> responses = sessionService.patchSession(therapist, sessionId, scope, payload);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", responses)));
        } catch (SessionService.ConflictException e) {
            return ResponseEntity.status(409).body(ApiResponse.failure("CONFLICT", e.getMessage()));
        } catch (SessionService.ValidationException e) {
            return ResponseEntity.status(422).body(ApiResponse.failure("VALIDATION_ERROR", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<ApiResponse> cancelSession(
            HttpServletRequest request,
            @PathVariable UUID sessionId,
            @RequestParam(required = false, defaultValue = "single") String scope) {
        try {
            Tenant therapist = getAuthenticatedTenant(request);
            List<SessionResponse> responses = sessionService.cancelSession(therapist, sessionId, scope);
            return ResponseEntity.ok(ApiResponse.success(Map.of("data", responses)));
        } catch (SessionService.ValidationException e) {
            return ResponseEntity.status(422).body(ApiResponse.failure("VALIDATION_ERROR", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(ApiResponse.failure("UNAUTHORIZED", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.failure("INTERNAL_ERROR", e.getMessage()));
        }
    }
}
