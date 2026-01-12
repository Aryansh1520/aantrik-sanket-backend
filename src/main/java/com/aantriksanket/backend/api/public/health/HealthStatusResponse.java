package com.aantriksanket.backend.api.health;

import java.time.Instant;
import java.util.Map;

public class HealthStatusResponse {

    private final String status;
    private final Instant timestamp;
    private final Map<String, Object> components;

    public HealthStatusResponse(String status, Map<String, Object> components) {
        this.status = status;
        this.components = components;
        this.timestamp = Instant.now();
    }

    public String getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getComponents() {
        return components;
    }
}
