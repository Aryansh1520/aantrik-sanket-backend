package com.aantriksanket.backend.api.tenant.sessions;

import com.aantriksanket.backend.models.sessions.Session;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ConflictDetail {
    private UUID sessionId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    public ConflictDetail(Session session) {
        this.sessionId = session.getId();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
    }

    // Getters
    public UUID getSessionId() { return sessionId; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
}
