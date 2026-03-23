package com.aantriksanket.backend.api.tenant.sessions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class UpdateSessionRequest {

    private String type;
    private BigDecimal price;
    private String location;
    private OffsetDateTime startTime;
    private Integer durationMinutes;

    // For future updates
    private Integer count;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
