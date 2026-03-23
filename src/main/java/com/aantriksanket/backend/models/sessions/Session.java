package com.aantriksanket.backend.models.sessions;

import com.aantriksanket.backend.models.Client;
import com.aantriksanket.backend.models.Tenant;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions", indexes = {
    @Index(name = "idx_session_client_id", columnList = "client_id"),
    @Index(name = "idx_session_therapist_id", columnList = "therapist_id"),
    @Index(name = "idx_session_series_id", columnList = "series_id")
})
public class Session {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Tenant therapist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "start_time", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SessionType type;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "meet_link", columnDefinition = "TEXT")
    private String meetLink;

    @Column(name = "google_event_id", columnDefinition = "TEXT")
    private String googleEventId;

    @Column(name = "is_series_master", nullable = false)
    private Boolean isSeriesMaster = false;

    @Column(name = "original_start_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime originalStartTime;

    @Column(name = "original_end_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime originalEndTime;

    @Column(name = "reschedule_reason", columnDefinition = "TEXT")
    private String rescheduleReason;

    @Column(name = "canceled_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime canceledAt;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Series getSeries() { return series; }
    public void setSeries(Series series) { this.series = series; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Tenant getTherapist() { return therapist; }
    public void setTherapist(Tenant therapist) { this.therapist = therapist; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public SessionType getType() { return type; }
    public void setType(SessionType type) { this.type = type; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public String getGoogleEventId() { return googleEventId; }
    public void setGoogleEventId(String googleEventId) { this.googleEventId = googleEventId; }

    public Boolean getIsSeriesMaster() { return isSeriesMaster; }
    public void setIsSeriesMaster(Boolean seriesMaster) { isSeriesMaster = seriesMaster; }

    public OffsetDateTime getOriginalStartTime() { return originalStartTime; }
    public void setOriginalStartTime(OffsetDateTime originalStartTime) { this.originalStartTime = originalStartTime; }

    public OffsetDateTime getOriginalEndTime() { return originalEndTime; }
    public void setOriginalEndTime(OffsetDateTime originalEndTime) { this.originalEndTime = originalEndTime; }

    public String getRescheduleReason() { return rescheduleReason; }
    public void setRescheduleReason(String rescheduleReason) { this.rescheduleReason = rescheduleReason; }

    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(OffsetDateTime canceledAt) { this.canceledAt = canceledAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
