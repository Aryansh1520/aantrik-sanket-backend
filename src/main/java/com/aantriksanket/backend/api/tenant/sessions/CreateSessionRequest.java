package com.aantriksanket.backend.api.tenant.sessions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CreateSessionRequest {

    private UUID clientId;
    private String type; // GOOGLE_MEET or IN_PERSON
    private OffsetDateTime anchorStartTime;
    private Integer durationMinutes;
    private BigDecimal price;
    private String location;

    // Recurrence logic (if present, it's a recurring series)
    private Integer frequencyDays;
    private Integer totalCount;

    // Getters and Setters
    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public OffsetDateTime getAnchorStartTime() { return anchorStartTime; }
    public void setAnchorStartTime(OffsetDateTime anchorStartTime) { this.anchorStartTime = anchorStartTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getFrequencyDays() { return frequencyDays; }
    public void setFrequencyDays(Integer frequencyDays) { this.frequencyDays = frequencyDays; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
}
