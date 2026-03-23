package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<Client> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT c FROM Client c WHERE c.tenant.id = :tenantId AND " +
           "(LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Client> searchClients(@Param("tenantId") UUID tenantId, @Param("query") String query);
}
