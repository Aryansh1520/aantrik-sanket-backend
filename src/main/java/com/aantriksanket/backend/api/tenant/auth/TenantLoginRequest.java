package com.aantriksanket.backend.api.tenant.auth;

public class TenantLoginRequest {
    private String email;
    private String password;

    public TenantLoginRequest() {
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
}
