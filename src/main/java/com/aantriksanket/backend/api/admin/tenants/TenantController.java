package com.aantriksanket.backend.api.admin.tenants;

import com.aantriksanket.backend.service.admin.tenants.TenantService;
import com.aantriksanket.backend.util.ApiResponse;
import com.aantriksanket.backend.util.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenants")
@Tag(name = "Tenant Management")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @Operation(summary = "Create a new tenant")
    @RequiresPermission(category = "manage_tenants", method = "create")
    public ResponseEntity<ApiResponse> create(@RequestBody CreateTenantRequest request) {
        try {
            Map<String, Object> data = tenantService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @GetMapping
    @Operation(summary = "Get all tenants")
    @RequiresPermission(category = "manage_tenants", method = "read")
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<TenantResponse> tenants = tenantService.getAll();
            Map<String, Object> data = new HashMap<>();
            data.put("tenants", tenants);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID")
    @RequiresPermission(category = "manage_tenants", method = "read")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        try {
            TenantResponse response = tenantService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("tenant", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tenant")
    @RequiresPermission(category = "manage_tenants", method = "update")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody UpdateTenantRequest request) {
        try {
            TenantResponse response = tenantService.update(id, request);
            Map<String, Object> data = new HashMap<>();
            data.put("tenant", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tenant")
    @RequiresPermission(category = "manage_tenants", method = "delete")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            Map<String, Object> data = tenantService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }
}
