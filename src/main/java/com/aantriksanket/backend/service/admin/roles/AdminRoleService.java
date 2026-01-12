package com.aantriksanket.backend.service.admin.roles;

import com.aantriksanket.backend.api.admin.roles.AdminRoleRequest;
import com.aantriksanket.backend.api.admin.roles.AdminRoleResponse;
import com.aantriksanket.backend.models.AdminRole;
import com.aantriksanket.backend.models.AdminRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    public AdminRoleService(AdminRoleRepository adminRoleRepository) {
        this.adminRoleRepository = adminRoleRepository;
    }

    @Transactional
    public AdminRoleResponse create(AdminRoleRequest request) {
        if (adminRoleRepository.existsByRoleName(request.getRoleName())) {
            throw new RuntimeException("Role name already exists");
        }

        AdminRole role = new AdminRole(request.getRoleName(), request.getRolePermissions());
        role = adminRoleRepository.save(role);

        return new AdminRoleResponse(role.getId(), role.getRoleName(), role.getRolePermissions());
    }

    public List<AdminRoleResponse> getAll() {
        return adminRoleRepository.findAll().stream()
                .map(role -> new AdminRoleResponse(role.getId(), role.getRoleName(), role.getRolePermissions()))
                .collect(Collectors.toList());
    }

    public AdminRoleResponse getById(UUID id) {
        Optional<AdminRole> roleOpt = adminRoleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        AdminRole role = roleOpt.get();
        return new AdminRoleResponse(role.getId(), role.getRoleName(), role.getRolePermissions());
    }

    @Transactional
    public AdminRoleResponse update(UUID id, AdminRoleRequest request) {
        Optional<AdminRole> roleOpt = adminRoleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        AdminRole role = roleOpt.get();

        // Check if role name is being changed and if new name already exists
        if (!role.getRoleName().equals(request.getRoleName()) &&
            adminRoleRepository.existsByRoleName(request.getRoleName())) {
            throw new RuntimeException("Role name already exists");
        }

        role.setRoleName(request.getRoleName());
        role.setRolePermissions(request.getRolePermissions());
        role = adminRoleRepository.save(role);

        return new AdminRoleResponse(role.getId(), role.getRoleName(), role.getRolePermissions());
    }

    @Transactional
    public Map<String, Object> delete(UUID id) {
        Optional<AdminRole> roleOpt = adminRoleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Role not found");
        }

        // TODO: Check if any admin is using this role before deleting
        adminRoleRepository.deleteById(id);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Role deleted successfully");
        return data;
    }
}
