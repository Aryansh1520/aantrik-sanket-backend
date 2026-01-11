package com.aantriksanket.backend.api.filter;

import com.aantriksanket.backend.config.RateLimitProperties;
import com.aantriksanket.backend.service.ratelimit.InMemoryRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final RateLimitProperties properties;

    public RateLimitFilter(
            InMemoryRateLimiter rateLimiter,
            RateLimitProperties properties
    ) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientKey = resolveClientKey(request);

        boolean allowed = rateLimiter.allow(
                clientKey,
                properties.getRequests(),
                properties.getWindowSeconds()
        );

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                  "error": "Too many requests"
                }
            """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        // Prefer proxy / load balancer header
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        String ip = request.getRemoteAddr();

        // Normalize IPv6 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }
}
