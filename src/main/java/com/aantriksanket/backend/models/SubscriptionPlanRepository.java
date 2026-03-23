package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByName(String name);
    boolean existsByName(String name);

    // features/permissions is a scalar jsonb column — no JOIN FETCH needed; use findById directly
    default Optional<SubscriptionPlan> findByIdWithFeatures(UUID id) {
        return findById(id);
    }
}
