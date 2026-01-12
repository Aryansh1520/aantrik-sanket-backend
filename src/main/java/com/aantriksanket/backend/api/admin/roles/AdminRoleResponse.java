package com.aantriksanket.backend.api.admin.roles;

import java.util.Map;
import java.util.UUID;

public class AdminRoleResponse {
    private UUID id;
    private String roleName;
    private Map<String, Map<String, Boolean>> rolePermissions;

    public AdminRoleResponse() {
    }

    public AdminRoleResponse(UUID id, String roleName, Map<String, Map<String, Boolean>> rolePermissions) {
        this.id = id;
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
