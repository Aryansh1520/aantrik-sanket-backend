package com.aantriksanket.backend.api.tenant.sessions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Request payload to create a single session or a recurrent series of sessions")
public class CreateSessionRequest {

    @Schema(description = "UUID of the client", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID clientId;
    
    @Schema(description = "The type of session (e.g., GOOGLE_MEET, IN_PERSON, PHONE)", example = "GOOGLE_MEET", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type; 
    
    @Schema(description = "The start time of the first session in the series", example = "2026-03-24T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private OffsetDateTime anchorStartTime;
    
    @Schema(description = "Duration of the session in minutes", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer durationMinutes;
    
    @Schema(description = "Price of the session", example = "100.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal price;
    
    @Schema(description = "Location for IN_PERSON sessions, or any specific notes", example = "Clinic Room A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    @Schema(description = "If recurrent, the number of days between each session (e.g., 7 for weekly). Leave null or 0 for a single session.", example = "7", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer frequencyDays;
    
    @Schema(description = "If recurrent, the total number of sessions to generate in this series. Leave null or 0 for a single session.", example = "4", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer totalCount;

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
