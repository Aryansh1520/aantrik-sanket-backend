package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT sp FROM SubscriptionPlan sp LEFT JOIN FETCH sp.features WHERE sp.id = :id")
    Optional<SubscriptionPlan> findByIdWithFeatures(@Param("id") UUID id);
}
