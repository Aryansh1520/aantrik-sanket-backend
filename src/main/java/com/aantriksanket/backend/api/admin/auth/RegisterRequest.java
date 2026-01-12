package com.aantriksanket.backend.api.admin.auth;

import java.util.UUID;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private UUID roleId;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String fullName, UUID roleId) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
}
