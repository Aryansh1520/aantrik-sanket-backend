package com.aantriksanket.backend.service.admin.auth;

import com.aantriksanket.backend.api.admin.auth.LoginRequest;
import com.aantriksanket.backend.api.admin.auth.LoginResponse;
import com.aantriksanket.backend.api.admin.auth.RegisterRequest;
import com.aantriksanket.backend.api.admin.auth.ResetPasswordRequest;
import com.aantriksanket.backend.models.Admin;
import com.aantriksanket.backend.models.AdminRepository;
import com.aantriksanket.backend.models.AdminRole;
import com.aantriksanket.backend.models.AdminRoleRepository;
import com.aantriksanket.backend.util.security.JwtUtil;
import com.aantriksanket.backend.util.security.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminRepository adminRepository, AdminRoleRepository adminRoleRepository, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByEmailWithRole(request.getEmail());

        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        Admin admin = adminOpt.get();

        if (!admin.getIsActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!PasswordHasher.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail());

        return new LoginResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getFullName(),
                token,
                admin.getRole().getRoleName()
        );
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Email already exists");
            throw new RuntimeException("Email already exists");
        }

        Optional<AdminRole> roleOpt = adminRoleRepository.findById(request.getRoleId());
        if (roleOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid role");
            throw new RuntimeException("Invalid role");
        }

        String passwordHash = PasswordHasher.hash(request.getPassword());
        Admin admin = new Admin(
                request.getEmail(),
                passwordHash,
                request.getFullName(),
                roleOpt.get()
        );

        admin = adminRepository.save(admin);

        Map<String, Object> data = new HashMap<>();
        data.put("id", admin.getId());
        data.put("email", admin.getEmail());
        data.put("fullName", admin.getFullName());
        data.put("roleName", admin.getRole().getRoleName());

        return data;
    }

    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(request.getEmail());

        if (adminOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Admin not found");
            throw new RuntimeException("Admin not found");
        }

        Admin admin = adminOpt.get();
        String newPasswordHash = PasswordHasher.hash(request.getNewPassword());
        admin.setPasswordHash(newPasswordHash);
        adminRepository.save(admin);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Password reset successfully");

        return data;
    }
}
