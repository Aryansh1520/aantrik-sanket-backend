package com.aantriksanket.backend.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);

    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.role WHERE a.id = :id")
    Optional<Admin> findByIdWithRole(@Param("id") UUID id);

    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.role WHERE a.email = :email")
    Optional<Admin> findByEmailWithRole(@Param("email") String email);

    boolean existsByEmail(String email);
    long count();
}
