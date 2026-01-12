package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, UUID> {
    Optional<AdminRole> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
    long count();
}
