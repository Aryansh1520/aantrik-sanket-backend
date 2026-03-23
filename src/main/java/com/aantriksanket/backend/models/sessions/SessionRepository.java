package com.aantriksanket.backend.models.sessions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("SELECT s FROM Session s WHERE s.therapist.id = :therapistId " +
           "AND (:excludeIds IS NULL OR s.id NOT IN :excludeIds) " +
           "AND s.startTime < :endTime AND s.endTime > :startTime " +
           "AND s.status != 'CANCELED'")
    List<Session> findConflicts(
            @Param("therapistId") UUID therapistId,
            @Param("excludeIds") List<UUID> excludeIds,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );

    List<Session> findBySeriesIdOrderByStartTimeAsc(UUID seriesId);
}
