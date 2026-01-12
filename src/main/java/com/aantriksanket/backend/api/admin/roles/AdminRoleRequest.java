package com.aantriksanket.backend.api.admin.roles;

import java.util.Map;

public class AdminRoleRequest {
    private String roleName;
    private Map<String, Map<String, Boolean>> rolePermissions;

    public AdminRoleRequest() {
    }

    public AdminRoleRequest(String roleName, Map<String, Map<String, Boolean>> rolePermissions) {
        this.roleName = roleName;
        this.rolePermissions = rolePermissions;
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
