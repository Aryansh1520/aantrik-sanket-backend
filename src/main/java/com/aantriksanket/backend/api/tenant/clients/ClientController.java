package com.aantriksanket.backend.api.tenant.clients;

import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.aantriksanket.backend.service.tenant.clients.ClientService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.TenantJwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenant/client")
@Tag(name = "Clients")
public class ClientController {

    private final ClientService clientService;
    private final TenantRepository tenantRepository;
    private final TenantJwtUtil tenantJwtUtil;

    public ClientController(ClientService clientService,
                            TenantRepository tenantRepository,
                            TenantJwtUtil tenantJwtUtil) {
        this.clientService = clientService;
        this.tenantRepository = tenantRepository;
        this.tenantJwtUtil = tenantJwtUtil;
    }

    // ──────────────────────────────────────────────
    // 1. CREATE CLIENT FROM PDF
    // ──────────────────────────────────────────────
    @PostMapping(value = "/create-client", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create client from consent form PDF")
    public ResponseEntity<ApiResponse> createClientFromPdf(
            @Parameter(description = "Consent form PDF", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("pdf") MultipartFile pdf,
            HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            String clientId = clientService.createClientFromPdf(tenant, pdf);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "success");
            data.put("client_id", clientId);
            data.put("message", "Client successfully created from PDF");

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 2. LIST CLIENTS
    // ──────────────────────────────────────────────
    @GetMapping("/clients/list")
    @Operation(summary = "List all clients for the tenant")
    public ResponseEntity<ApiResponse> listClients(HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            List<ClientResponse> clients = clientService.listClients(tenant.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("status", "success");
            data.put("count", clients.size());
            data.put("clients", clients);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 3. SEARCH CLIENTS
    // ──────────────────────────────────────────────
    @GetMapping("/clients/search")
    @Operation(summary = "Search clients by full name, email, or phone number")
    public ResponseEntity<ApiResponse> searchClients(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            List<ClientResponse> clients = clientService.searchClients(tenant.getId(), query, limit);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "success");
            data.put("query", query);
            data.put("count", clients.size());
            data.put("clients", clients);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 4. GET CLIENT DETAILS
    // ──────────────────────────────────────────────
    @GetMapping("/clients/{clientId}")
    @Operation(summary = "Get detailed information for a specific client")
    public ResponseEntity<ApiResponse> getClientDetails(
            @PathVariable UUID clientId,
            HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            ClientResponse client = clientService.getClientDetails(tenant.getId(), clientId);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "success");
            data.put("client", client);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Usually "Client not found"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 5. UPDATE CLIENT
    // ──────────────────────────────────────────────
    @PutMapping("/clients/update/{clientId}")
    @Operation(summary = "Update client fields")
    public ResponseEntity<ApiResponse> updateClient(
            @PathVariable UUID clientId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            ClientResponse updatedClient = clientService.updateClient(tenant.getId(), clientId, body);

            Map<String, Object> data = new HashMap<>();
            data.put("status", "success");
            data.put("message", "Client updated successfully");
            data.put("client", updatedClient);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // 6. DOWNLOAD CONSENT FORM
    // ──────────────────────────────────────────────
    @GetMapping(value = "/clients/{clientId}/download-consent-form", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download the original consent form PDF for a client")
    public ResponseEntity<byte[]> downloadConsentForm(
            @PathVariable UUID clientId,
            HttpServletRequest request) {
        try {
            Tenant tenant = getAuthenticatedTenant(request);
            ClientResponse client = clientService.getClientDetails(tenant.getId(), clientId);
            byte[] pdfBytes = clientService.downloadConsentForm(tenant.getId(), clientId);

            String filename = client.getFullName() != null 
                    ? client.getFullName().replaceAll("\\s+", "_") + "_consent_form.pdf" 
                    : "consent_form.pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // ──────────────────────────────────────────────
    // Helper: Extract and Validate Tenant from JWT
    // ──────────────────────────────────────────────
    private Tenant getAuthenticatedTenant(HttpServletRequest request) {
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
            UUID tenantId = tenantJwtUtil.getTenantIdFromToken(token);
            return tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new UnauthorizedException("Tenant not found"));
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
