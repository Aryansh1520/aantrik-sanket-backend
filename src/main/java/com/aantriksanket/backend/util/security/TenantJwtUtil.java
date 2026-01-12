package com.aantriksanket.backend.util.security;

import com.aantriksanket.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Separate JWT util for tenants to ensure admin endpoints ignore tenant tokens.
 */
@Component
public class TenantJwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(TenantJwtUtil.class);
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public TenantJwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = createSecretKey(jwtProperties.getSecret() + "|tenant");
    }

    private SecretKey createSecretKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID tenantId, String email) {
        Date now = new Date();
        int minutes = jwtProperties.getExpireMinutes();
        if (minutes <= 0) {
            logger.warn("TenantJwtUtil: jwt.expire.minutes is <= 0 ({}). Falling back to 60 minutes.", minutes);
            minutes = 60;
        }
        Date expiryDate = new Date(now.getTime() + minutes * 60 * 1000L);
        logger.debug("TenantJwtUtil: Generating token for tenantId={}, email={}, issuedAt={}, expiresAt={}, minutes={}",
                tenantId, email, now, expiryDate, minutes);

        return Jwts.builder()
                .subject(tenantId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public UUID getTenantIdFromToken(String token) {
        return UUID.fromString(getClaimFromToken(token, Claims::getSubject));
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new RuntimeException("Token has expired", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new RuntimeException("Invalid token signature", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("Malformed token", e);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing token: " + e.getMessage(), e);
        }
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}
