package com.aantriksanket.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "admin_roles")
public class AdminRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "role_permissions", nullable = false, columnDefinition = "jsonb")
    private Map<String, Map<String, Boolean>> rolePermissions;

    public AdminRole() {
    }

    public AdminRole(String roleName, Map<String, Map<String, Boolean>> rolePermissions) {
        this.roleName = roleName;
        this.rolePermissions = rolePermissions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Map<String, Map<String, Boolean>> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(Map<String, Map<String, Boolean>> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}
