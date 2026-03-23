package com.aantriksanket.backend.api.tenant.sessions;

import com.aantriksanket.backend.models.sessions.Session;
import com.aantriksanket.backend.models.sessions.SyncStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SessionResponse {
    private UUID id;
    private UUID seriesId;
    private UUID clientId;
    private String status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer durationMinutes;
    private String type;
    private BigDecimal price;
    private String location;
    private String meetLink;
    private Boolean isSeriesMaster;
    private OffsetDateTime originalStartTime;
    private OffsetDateTime originalEndTime;
    private String rescheduleReason;
    private OffsetDateTime canceledAt;
    
    // Extracted from related CalendarEvent
    private String syncStatus;

    public SessionResponse(Session session, SyncStatus syncStatus) {
        this.id = session.getId();
        this.seriesId = session.getSeries() != null ? session.getSeries().getId() : null;
        this.clientId = session.getClient().getId();
        this.status = session.getStatus().name();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.durationMinutes = session.getDurationMinutes();
        this.type = session.getType().name();
        this.price = session.getPrice();
        this.location = session.getLocation();
        this.meetLink = session.getMeetLink();
        this.isSeriesMaster = session.getIsSeriesMaster();
        this.originalStartTime = session.getOriginalStartTime();
        this.originalEndTime = session.getOriginalEndTime();
        this.rescheduleReason = session.getRescheduleReason();
        this.canceledAt = session.getCanceledAt();
        this.syncStatus = syncStatus != null ? syncStatus.name() : "PENDING";
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSeriesId() { return seriesId; }
    public UUID getClientId() { return clientId; }
    public String getStatus() { return status; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public String getLocation() { return location; }
    public String getMeetLink() { return meetLink; }
    public Boolean getIsSeriesMaster() { return isSeriesMaster; }
    public OffsetDateTime getOriginalStartTime() { return originalStartTime; }
    public OffsetDateTime getOriginalEndTime() { return originalEndTime; }
    public String getRescheduleReason() { return rescheduleReason; }
    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public String getSyncStatus() { return syncStatus; }
}
