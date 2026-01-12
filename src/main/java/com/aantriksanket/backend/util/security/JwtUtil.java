package com.aantriksanket.backend.util.security;

import com.aantriksanket.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = createSecretKey(jwtProperties.getSecret());
    }

    private SecretKey createSecretKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // JWT requires at least 256 bits (32 bytes) for HMAC-SHA algorithms
        if (keyBytes.length < 32) {
            // Use SHA-256 to hash the secret to ensure it's at least 32 bytes
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID adminId, String email) {
        Date now = new Date();
        int minutes = jwtProperties.getExpireMinutes();
        if (minutes <= 0) {
            logger.warn("JwtUtil: jwt.expire.minutes is <= 0 ({}). Falling back to 60 minutes.", minutes);
            minutes = 60;
        }
        Date expiryDate = new Date(now.getTime() + minutes * 60 * 1000L);
        logger.debug("JwtUtil: Generating token for adminId={}, email={}, issuedAt={}, expiresAt={}, minutes={}",
                adminId, email, now, expiryDate, minutes);

        return Jwts.builder()
                .subject(adminId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public UUID getAdminIdFromToken(String token) {
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
