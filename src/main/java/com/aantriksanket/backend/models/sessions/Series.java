package com.aantriksanket.backend.models.sessions;

import com.aantriksanket.backend.models.Client;
import com.aantriksanket.backend.models.Tenant;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "series", indexes = {
    @Index(name = "idx_series_client_id", columnList = "client_id"),
    @Index(name = "idx_series_therapist_id", columnList = "therapist_id")
})
public class Series {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Tenant therapist;

    @Column(name = "frequency_days")
    private Integer frequencyDays;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "anchor_start_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime anchorStartTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SessionType type;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "meet_link", columnDefinition = "TEXT")
    private String meetLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SeriesStatus status = SeriesStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Tenant getTherapist() { return therapist; }
    public void setTherapist(Tenant therapist) { this.therapist = therapist; }

    public Integer getFrequencyDays() { return frequencyDays; }
    public void setFrequencyDays(Integer frequencyDays) { this.frequencyDays = frequencyDays; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public OffsetDateTime getAnchorStartTime() { return anchorStartTime; }
    public void setAnchorStartTime(OffsetDateTime anchorStartTime) { this.anchorStartTime = anchorStartTime; }

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

    public SeriesStatus getStatus() { return status; }
    public void setStatus(SeriesStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
