package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, UUID> {
    @Query("SELECT sh FROM SubscriptionHistory sh " +
           "LEFT JOIN FETCH sh.tenant " +
           "LEFT JOIN FETCH sh.subscriptionPlan " +
           "WHERE sh.tenant.id = :tenantId " +
           "ORDER BY sh.createdAt DESC")
    List<SubscriptionHistory> findByTenantIdOrderByCreatedAtDesc(@Param("tenantId") UUID tenantId);
}
