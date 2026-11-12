package org.bcp.dlmsenrolment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.AssignPermissionToRoleRequestDto;
import org.bcp.dlmsenrolment.dtos.request.RoleCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.RoleUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.RoleResponseDto;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.entities.Role;
import org.bcp.dlmsenrolment.entities.RolePermission;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.bcp.dlmsenrolment.repositories.RolePermissionRepository;
import org.bcp.dlmsenrolment.repositories.RoleRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RoleService - Business logic for role management.
 * 
 * CRUD operations:
 * - CREATE: Create new role
 * - READ: Get role by ID or name
 * - UPDATE: Update role details
 * - DELETE: Soft-delete role
 * 
 * Permission management:
 * - ASSIGN: Assign permission to role
 * - REVOKE: Remove permission from role
 * - LIST: Get role's permissions
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Create a new role
     */
    public RoleResponseDto createRole(RoleCreateRequestDto dto) {
        log.info("Creating new role: {}", dto.getRoleName());

        // Check if role already exists
        if (roleRepository.existsByRoleName(dto.getRoleName())) {
            throw new RuntimeException("Role with name '" + dto.getRoleName() + "' already exists");
        }

        // Create new role
        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Save
        Role saved = roleRepository.save(role);
        log.info("Role created successfully: {}", saved.getId());

        return mapToResponseDto(saved);
    }

    /**
     * Get role by ID
     */
    public RoleResponseDto getRoleById(Long id) {
        return roleRepository.findById(id)
            .filter(r -> !r.isDeleted())
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }

    /**
     * Get role by name
     */
    public RoleResponseDto getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

    /**
     * Get all active roles
     */
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAllForAdmin()
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Update role details
     */
    public RoleResponseDto updateRole(Long id, RoleUpdateRequestDto dto, String updatedByUsername) {
        log.info("Updating role: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            if (dto.getIsActive()) {
                role.activate();
            } else {
                deactivateRole(role, updatedByUsername);
            }
        }

        Role updated = roleRepository.save(role);
        log.info("Role updated successfully: {}", id);

        return mapToResponseDto(updated);
    }

    /**
     * Soft-delete role
     */
    public void deleteRole(Long id, String deactivatedByUsername) {
        log.info("Deactivating role: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        deactivateRole(role, deactivatedByUsername);
        roleRepository.save(role);
        log.info("Role deactivated successfully: {}", id);
    }

    /**
     * Restore a soft-deleted role
     */
    public void restoreRole(Long id) {
        log.info("Restoring role: {}", id);

        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if (!role.isDeleted()) {
            throw new RuntimeException("Role is not deleted: " + id);
        }

        role.restore();
        roleRepository.save(role);
        log.info("Role restored successfully: {}", id);
    }

    /**
     * Assign a permission to a role
     */
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));

        if (role.isDeleted() || !Boolean.TRUE.equals(role.getIsActive())) {
            throw new RuntimeException("Cannot assign permission to inactive role");
        }

        if (permission.isDeleted()) {
            throw new RuntimeException("Cannot assign deleted permission");
        }

        // Check if already assigned
        if (rolePermissionRepository.roleHasPermission(roleId, permissionId)) {
            throw new RuntimeException("Role already has this permission");
        }

        // Create new role-permission mapping
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        rolePermissionRepository.save(rolePermission);
        log.info("Permission assigned successfully: role {} <- permission {}", roleId, permissionId);
    }

    /**
     * Remove a permission from a role
     */
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        RolePermission rolePermission = rolePermissionRepository.findByRoleAndPermission(roleId, permissionId)
            .orElseThrow(() -> new RuntimeException("Role-permission assignment not found"));

        rolePermissionRepository.delete(rolePermission);
        log.info("Permission removed successfully: role {} -> permission {}", roleId, permissionId);
    }

    /**
     * Get all permissions for a role
     */
    public List<String> getRolePermissions(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId)
            .stream()
            .map(rp -> rp.getPermission().getPermissionName())
            .collect(Collectors.toList());
    }

    /**
     * Map Role entity to response DTO
     */
    private RoleResponseDto mapToResponseDto(Role role) {
        List<String> permissionNames = rolePermissionRepository.findByRoleId(role.getId())
            .stream()
            .map(rp -> rp.getPermission().getPermissionName())
            .collect(Collectors.toList());

        return RoleResponseDto.builder()
            .id(role.getId())
            .roleName(role.getRoleName())
            .description(role.getDescription())
            .isActive(role.getIsActive())
            .permissionNames(permissionNames.stream().collect(Collectors.toSet()))
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .deletedAt(role.getDeletedAt())
            .isDeleted(role.isDeleted())
            .build();
    }

    private void deactivateRole(Role role, String deactivatedByUsername) {
        role.deactivate();
        userRoleRepository.findUsersByRoleId(role.getId())
            .forEach(userRole -> userRole.revoke(deactivatedByUsername));
    }
}
