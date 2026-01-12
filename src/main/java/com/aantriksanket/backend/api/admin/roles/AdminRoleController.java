package com.aantriksanket.backend.api.admin.roles;

import com.aantriksanket.backend.service.admin.roles.AdminRoleService;
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
@RequestMapping("/api/admin/roles")
@Tag(name = "Admin Roles")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }

    @PostMapping
    @Operation(summary = "Create a new admin role")
    @RequiresPermission(category = "roles", method = "create")
    public ResponseEntity<ApiResponse> create(@RequestBody AdminRoleRequest request) {
        try {
            AdminRoleResponse response = adminRoleService.create(request);
            Map<String, Object> data = new HashMap<>();
            data.put("role", response);
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
    @Operation(summary = "Get all admin roles")
    @RequiresPermission(category = "roles", method = "read")
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<AdminRoleResponse> roles = adminRoleService.getAll();
            Map<String, Object> data = new HashMap<>();
            data.put("roles", roles);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get admin role by ID")
    @RequiresPermission(category = "roles", method = "read")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        try {
            AdminRoleResponse response = adminRoleService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("role", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an admin role")
    @RequiresPermission(category = "roles", method = "update")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody AdminRoleRequest request) {
        try {
            AdminRoleResponse response = adminRoleService.update(id, request);
            Map<String, Object> data = new HashMap<>();
            data.put("role", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an admin role")
    @RequiresPermission(category = "roles", method = "delete")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            Map<String, Object> data = adminRoleService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }
}
