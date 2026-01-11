package com.aantriksanket.backend.api.health;

import com.aantriksanket.backend.service.health.DatabaseHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Get system health status")
    public HealthStatusResponse getHealth() {

        Map<String, Object> components = new HashMap<>();
        components.put("database", databaseHealthService.check());

        boolean allUp = "UP".equals(
                ((Map<?, ?>) components.get("database")).get("status")
        );

        return new HealthStatusResponse(
                allUp ? "UP" : "DEGRADED",
                components
        );
    }
}
