package org.bcp.dlmsenrolment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PermissionCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.PermissionUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PermissionResponseDto;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PermissionService - Business logic for permission management.
 * 
 * CRUD operations:
 * - CREATE: Create new permission
 * - READ: Get permission by ID or name
 * - UPDATE: Update permission details
 * - DELETE: Soft-delete permission
 * 
 * Advanced:
 * - GET: Permissions by endpoint/method
 * - GET: Permissions requiring owner check
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Create a new permission
     */
    public PermissionResponseDto createPermission(PermissionCreateRequestDto dto) {
        log.info("Creating new permission: {}", dto.getPermissionName());

        // Check if permission already exists
        if (permissionRepository.existsByPermissionNameActive(dto.getPermissionName())) {
            throw new RuntimeException("Permission with name '" + dto.getPermissionName() + "' already exists");
        }

        // Create new permission
        Permission permission = new Permission();
        permission.setPermissionName(dto.getPermissionName());
        permission.setEndpoint(dto.getEndpoint());
        permission.setHttpMethod(dto.getHttpMethod().toUpperCase());
        permission.setDescription(dto.getDescription());
        permission.setRequiresOwnerCheck(dto.getRequiresOwnerCheck() != null ? dto.getRequiresOwnerCheck() : false);
        permission.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Save
        Permission saved = permissionRepository.save(permission);
        log.info("Permission created successfully: {}", saved.getId());

        return mapToResponseDto(saved);
    }

    /**
     * Get permission by ID
     */
    public PermissionResponseDto getPermissionById(Long id) {
        return permissionRepository.findById(id)
            .filter(p -> !p.isDeleted())
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + id));
    }

    /**
     * Get permission by name
     */
    public PermissionResponseDto getPermissionByName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName)
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));
    }

    /**
     * Get all active permissions
     */
    public List<PermissionResponseDto> getAllPermissions() {
        return permissionRepository.findAllActive()
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Get permissions by endpoint and HTTP method
     */
    public List<PermissionResponseDto> getPermissionsByEndpointAndMethod(String endpoint, String httpMethod) {
        return permissionRepository.findByEndpointAndMethod(endpoint, httpMethod.toUpperCase())
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all permissions that require owner check
     */
    public List<PermissionResponseDto> getPermissionsRequiringOwnerCheck() {
        return permissionRepository.findPermissionsRequiringOwnerCheck()
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Update permission details
     */
    public PermissionResponseDto updatePermission(Long id, PermissionUpdateRequestDto dto) {
        log.info("Updating permission: {}", id);

        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + id));

        if (permission.isDeleted()) {
            throw new RuntimeException("Cannot update deleted permission: " + id);
        }

        if (dto.getDescription() != null) {
            permission.setDescription(dto.getDescription());
        }
        if (dto.getRequiresOwnerCheck() != null) {
            permission.setRequiresOwnerCheck(dto.getRequiresOwnerCheck());
        }
        if (dto.getIsActive() != null) {
            permission.setIsActive(dto.getIsActive());
        }

        Permission updated = permissionRepository.save(permission);
        log.info("Permission updated successfully: {}", id);

        return mapToResponseDto(updated);
    }

    /**
     * Soft-delete permission
     */
    public void deletePermission(Long id) {
        log.warn("Deleting permission: {}", id);

        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + id));

        if (permission.isDeleted()) {
            throw new RuntimeException("Permission already deleted: " + id);
        }

        permission.softDelete();
        permissionRepository.save(permission);
        log.info("Permission soft-deleted successfully: {}", id);
    }

    /**
     * Restore a soft-deleted permission
     */
    public void restorePermission(Long id) {
        log.info("Restoring permission: {}", id);

        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + id));

        if (!permission.isDeleted()) {
            throw new RuntimeException("Permission is not deleted: " + id);
        }

        permission.restore();
        permissionRepository.save(permission);
        log.info("Permission restored successfully: {}", id);
    }

    /**
     * Map Permission entity to response DTO
     */
    private PermissionResponseDto mapToResponseDto(Permission permission) {
        return PermissionResponseDto.builder()
            .id(permission.getId())
            .permissionName(permission.getPermissionName())
            .endpoint(permission.getEndpoint())
            .httpMethod(permission.getHttpMethod())
            .description(permission.getDescription())
            .requiresOwnerCheck(permission.getRequiresOwnerCheck())
            .isActive(permission.getIsActive())
            .createdAt(permission.getCreatedAt())
            .updatedAt(permission.getUpdatedAt())
            .deletedAt(permission.getDeletedAt())
            .isDeleted(permission.isDeleted())
            .build();
    }
}
