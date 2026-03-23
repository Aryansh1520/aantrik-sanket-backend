package com.aantriksanket.backend.api.tenant.sessions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Schema(description = "Payload to update an existing session. All fields are optional and only provided fields will be updated.")
public class UpdateSessionRequest {

    @Schema(description = "New start time for the session (and subsequent sessions if scope=future_in_series). Calculates an offset delta to apply to the future series.", example = "2026-03-24T10:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private OffsetDateTime startTime;
    
    @Schema(description = "New duration of the session in minutes", example = "90", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer durationMinutes;
    
    @Schema(description = "New session type (GOOGLE_MEET, IN_PERSON, PHONE)", example = "IN_PERSON", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String type; 
    
    @Schema(description = "New price", example = "120.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal price;
    
    @Schema(description = "New location", example = "Clinic Room B", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String location;

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
