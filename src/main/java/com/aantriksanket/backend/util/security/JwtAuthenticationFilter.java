package com.aantriksanket.backend.util.security;

import com.aantriksanket.backend.models.Admin;
import com.aantriksanket.backend.models.AdminRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final AdminRepository adminRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AdminRepository adminRepository) {
        this.jwtUtil = jwtUtil;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        logger.debug("JwtAuthenticationFilter: Processing request to {}", request.getRequestURI());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                String email = jwtUtil.getEmailFromToken(token);
                UUID adminId = jwtUtil.getAdminIdFromToken(token);
                logger.debug("JwtAuthenticationFilter: Extracted email={}, adminId={}", email, adminId);

                if (jwtUtil.validateToken(token, email)) {
                    logger.debug("JwtAuthenticationFilter: Token validated, loading admin with role");
                    Optional<Admin> adminOpt = adminRepository.findByIdWithRole(adminId);
                    if (adminOpt.isPresent()) {
                        Admin admin = adminOpt.get();
                        logger.debug("JwtAuthenticationFilter: Admin loaded, id={}, email={}, isActive={}",
                                admin.getId(), admin.getEmail(), admin.getIsActive());

                        if (admin.getIsActive()) {
                            if (admin.getRole() != null) {
                                logger.debug("JwtAuthenticationFilter: Admin role loaded, roleId={}, roleName={}, permissions={}",
                                        admin.getRole().getId(),
                                        admin.getRole().getRoleName(),
                                        admin.getRole().getRolePermissions());
                            } else {
                                logger.error("JwtAuthenticationFilter: Admin role is NULL for adminId={}", admin.getId());
                            }

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            admin,
                                            null,
                                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                    );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("JwtAuthenticationFilter: Authentication set in SecurityContext");
                        } else {
                            logger.warn("JwtAuthenticationFilter: Admin is inactive, id={}", admin.getId());
                        }
                    } else {
                        logger.warn("JwtAuthenticationFilter: Admin not found for id={}", adminId);
                    }
                } else {
                    logger.warn("JwtAuthenticationFilter: Token validation failed for email={}", email);
                }
            } catch (Exception e) {
                logger.error("JwtAuthenticationFilter: Exception processing token - {}", e.getMessage(), e);
                logger.error("JwtAuthenticationFilter: Exception class: {}", e.getClass().getName());
                if (e.getCause() != null) {
                    logger.error("JwtAuthenticationFilter: Caused by: {}", e.getCause().getMessage());
                }
                // Token is invalid or expired
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("JwtAuthenticationFilter: No Authorization header found");
        }

        filterChain.doFilter(request, response);
    }
}
