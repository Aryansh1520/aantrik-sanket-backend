package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findByPhoneNumber(String phoneNumber);
    Optional<Tenant> findByDomainName(String domainName);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByDomainName(String domainName);

    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.subscriptionPlan WHERE t.id = :id")
    Optional<Tenant> findByIdWithSubscription(@Param("id") UUID id);

    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.subscriptionPlan WHERE t.email = :email")
    Optional<Tenant> findByEmailWithSubscription(@Param("email") String email);
}
