package com.aantriksanket.backend.service.tenant.google;

import com.aantriksanket.backend.config.GoogleOAuthProperties;
import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TenantGoogleOAuthService {

    private static final List<String> SCOPES = Arrays.asList(
            "openid",
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    private final GoogleOAuthProperties googleOAuthProperties;
    private final TenantRepository tenantRepository;

    public TenantGoogleOAuthService(GoogleOAuthProperties googleOAuthProperties,
                                     TenantRepository tenantRepository) {
        this.googleOAuthProperties = googleOAuthProperties;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Build and return the Google OAuth authorization URL.
     * The tenantId is encoded as the `state` parameter so we can link the callback back to the tenant.
     */
    public String buildAuthorizationUrl(UUID tenantId) {
        try {
            GoogleAuthorizationCodeFlow flow = buildFlow();
            return flow.newAuthorizationUrl()
                    .setRedirectUri(googleOAuthProperties.getRedirectUri())
                    .setAccessType("offline")
                    .setApprovalPrompt("force")
                    .setState(tenantId.toString())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Google OAuth URL: " + e.getMessage(), e);
        }
    }

    /**
     * Exchange the authorization code for tokens and persist them on the Tenant.
     */
    @Transactional
    public void handleCallback(String code, String state) {
        UUID tenantId;
        try {
            tenantId = UUID.fromString(state);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid OAuth state — could not resolve tenant.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found for OAuth state: " + state));

        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    googleOAuthProperties.getClientId(),
                    googleOAuthProperties.getClientSecret(),
                    code,
                    googleOAuthProperties.getRedirectUri()
            ).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Google token exchange failed: " + e.getMessage(), e);
        }

        // Extract the Google email from the ID token payload
        String googleEmail = null;
        try {
            if (tokenResponse.getIdToken() != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken =
                        tokenResponse.parseIdToken();
                if (idToken != null && idToken.getPayload() != null) {
                    googleEmail = idToken.getPayload().getEmail();
                }
            }
        } catch (Exception e) {
            // Non-fatal: email is optional
        }

        // Calculate token expiry
        Long expiresInSeconds = tokenResponse.getExpiresInSeconds();
        OffsetDateTime tokenExpiry = expiresInSeconds != null
                ? OffsetDateTime.now().plusSeconds(expiresInSeconds)
                : null;

        tenant.setGoogleAccessToken(tokenResponse.getAccessToken());
        if (tokenResponse.getRefreshToken() != null) {
            tenant.setGoogleRefreshToken(tokenResponse.getRefreshToken());
        }
        tenant.setGoogleTokenExpiry(tokenExpiry);
        tenant.setGoogleEmail(googleEmail);
        tenant.setGoogleCalendarId("primary");

        tenantRepository.save(tenant);
    }

    /**
     * Returns the Google connection status for the given tenant.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatus(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Map<String, Object> status = new HashMap<>();
        status.put("connected", tenant.getGoogleAccessToken() != null && !tenant.getGoogleAccessToken().isBlank());
        status.put("google_email", tenant.getGoogleEmail());
        status.put("google_calendar_id", tenant.getGoogleCalendarId());
        status.put("token_expiry", tenant.getGoogleTokenExpiry());
        return status;
    }

    /**
     * Clears all Google OAuth tokens and resets the calendar ID to "primary".
     */
    @Transactional
    public void disconnect(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        tenant.setGoogleAccessToken(null);
        tenant.setGoogleRefreshToken(null);
        tenant.setGoogleTokenExpiry(null);
        tenant.setGoogleEmail(null);
        tenant.setGoogleCalendarId("primary");

        tenantRepository.save(tenant);
    }

    private GoogleAuthorizationCodeFlow buildFlow() throws GeneralSecurityException, IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                googleOAuthProperties.getClientId(),
                googleOAuthProperties.getClientSecret(),
                SCOPES
        ).setAccessType("offline").build();
    }
}
