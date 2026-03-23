package com.aantriksanket.backend.service.sessions;

import com.aantriksanket.backend.config.GoogleOAuthProperties;
import com.aantriksanket.backend.models.Tenant;
import com.aantriksanket.backend.models.TenantRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;

@Service
public class GoogleCredentialService {

    private final GoogleOAuthProperties properties;
    private final TenantRepository tenantRepository;

    public GoogleCredentialService(GoogleOAuthProperties properties, TenantRepository tenantRepository) {
        this.properties = properties;
        this.tenantRepository = tenantRepository;
    }

    public Credential getCredential(Tenant tenant) throws GeneralSecurityException, IOException {
        if (tenant.getGoogleRefreshToken() == null) {
            throw new IllegalStateException("Tenant is not connected to Google Calendar");
        }

        Credential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .addRefreshListener(new CredentialRefreshListener() {
                    @Override
                    public void onTokenResponse(Credential credential, TokenResponse tokenResponse) {
                        tenant.setGoogleAccessToken(credential.getAccessToken());
                        if (credential.getRefreshToken() != null) {
                            tenant.setGoogleRefreshToken(credential.getRefreshToken());
                        }
                        if (credential.getExpiresInSeconds() != null) {
                            tenant.setGoogleTokenExpiry(OffsetDateTime.now().plusSeconds(credential.getExpiresInSeconds()));
                        }
                        tenantRepository.save(tenant);
                    }

                    @Override
                    public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse) {
                        // In a production scenario, we might want to flag the tenant's connection as broken
                        System.err.println("Failed to refresh Google token for tenant " + tenant.getId() + ": " + tokenErrorResponse.getError());
                    }
                })
                .build();

        credential.setAccessToken(tenant.getGoogleAccessToken());
        credential.setRefreshToken(tenant.getGoogleRefreshToken());
        if (tenant.getGoogleTokenExpiry() != null) {
            credential.setExpirationTimeMilliseconds(tenant.getGoogleTokenExpiry().toInstant().toEpochMilli());
        }

        return credential;
    }
}
