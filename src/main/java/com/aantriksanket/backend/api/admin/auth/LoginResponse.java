package com.aantriksanket.backend.api.admin.auth;

import java.util.UUID;

public class LoginResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String token;
    private String roleName;

    public LoginResponse() {
    }

    public LoginResponse(UUID id, String email, String fullName, String token, String roleName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.roleName = roleName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
