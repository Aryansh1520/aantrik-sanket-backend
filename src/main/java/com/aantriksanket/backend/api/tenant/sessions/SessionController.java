package com.aantriksanket.backend.api.tenant.sessions;

import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.aantriksanket.backend.service.sessions.SessionService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.TenantJwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenant/sessions")
@Tag(name = "Sessions (Therapist)", description = "APIs for therapists to manage therapy sessions, recurrent series, and Google Calendar synchronization.")
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

    @Operation(
            summary = "Create a new session or recurrent series",
            description = "Creates a single session or a recurrent series. Performs single-query DB collision detection before saving. If frequencyDays and totalCount are provided, a Series entity is created mapping all N sessions into the future. Each session triggers an asynchronous JobRunr task to sync with Google Calendar."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sessions created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict detected in one or more proposed session times. Detail contains the conflicting existing session.", content = @Content(schema = @Schema(implementation = com.aantriksanket.backend.util.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error (e.g. scheduling past session, client unauthorized)")
    })
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

    @Operation(summary = "List sessions for therapist", description = "Retrieves all future sessions for the authenticated therapist. Provide clientId to filter down to a specific client. Use includeCanceled to view CANCELED status sessions.")
    @GetMapping
    public ResponseEntity<ApiResponse> getSessions(
            HttpServletRequest request,
            @Parameter(description = "Optional client UUID to filter sessions by") @RequestParam(required = false) UUID clientId,
            @Parameter(description = "Whether to include canceled sessions in the response") @RequestParam(required = false, defaultValue = "false") Boolean includeCanceled) {
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

    @Operation(summary = "Get specific session details", description = "Retrieves details of a single session including Google Calendar sync status.")
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

    @Operation(summary = "Get all sessions in a specific series", description = "Returns all sessions sequentially mapping to the SeriesId provided.")
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

    @Operation(
            summary = "Patch update a session",
            description = "Update details for a single session or all future occurrences in a series. Use scope='single' (default) to only edit this exact session, or scope='future_in_series' to apply edits forward starting from this session's time."
    )
    @PatchMapping("/{sessionId}")
    public ResponseEntity<ApiResponse> patchSession(
            HttpServletRequest request,
            @PathVariable UUID sessionId,
            @Parameter(description = "Scope of update: 'single' or 'future_in_series'") @RequestParam(required = false, defaultValue = "single") String scope,
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

    @Operation(
            summary = "Cancel session",
            description = "Triggers a Google Calendar Event DELETE request asynchronously and sets the DB Session record to CANCELED. Can optionally apply forward to all future events using scope='future_in_series'."
    )
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<ApiResponse> cancelSession(
            HttpServletRequest request,
            @PathVariable UUID sessionId,
            @Parameter(description = "Scope of cancelation: 'single' or 'future_in_series'") @RequestParam(required = false, defaultValue = "single") String scope) {
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
