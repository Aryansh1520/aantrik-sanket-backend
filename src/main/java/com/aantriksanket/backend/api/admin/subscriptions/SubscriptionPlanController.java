package com.aantriksanket.backend.api.admin.subscriptions;

import com.aantriksanket.backend.service.admin.subscriptions.SubscriptionPlanService;
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
@RequestMapping("/api/admin/subscriptions")
@Tag(name = "Subscription Plans")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @PostMapping
    @Operation(summary = "Create a new subscription plan")
    @RequiresPermission(category = "subscriptions", method = "create")
    public ResponseEntity<ApiResponse> create(@RequestBody SubscriptionPlanRequest request) {
        try {
            SubscriptionPlanResponse response = subscriptionPlanService.create(request);
            Map<String, Object> data = new HashMap<>();
            data.put("subscription", response);
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
    @Operation(summary = "Get all subscription plans")
    @RequiresPermission(category = "subscriptions", method = "read")
    public ResponseEntity<ApiResponse> getAll() {
        try {
            List<SubscriptionPlanResponse> subscriptions = subscriptionPlanService.getAll();
            Map<String, Object> data = new HashMap<>();
            data.put("subscriptions", subscriptions);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription plan by ID")
    @RequiresPermission(category = "subscriptions", method = "read")
    public ResponseEntity<ApiResponse> getById(@PathVariable UUID id) {
        try {
            SubscriptionPlanResponse response = subscriptionPlanService.getById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("subscription", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a subscription plan")
    @RequiresPermission(category = "subscriptions", method = "update")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody SubscriptionPlanRequest request) {
        try {
            SubscriptionPlanResponse response = subscriptionPlanService.update(id, request);
            Map<String, Object> data = new HashMap<>();
            data.put("subscription", response);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subscription plan")
    @RequiresPermission(category = "subscriptions", method = "delete")
    public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
        try {
            Map<String, Object> data = subscriptionPlanService.delete(id);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(errorData));
        }
    }
}
