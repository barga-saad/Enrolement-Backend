package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.AssignPermissionToRoleRequestDto;
import org.bcp.dlmsenrolment.dtos.request.RoleCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.RoleUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.RoleResponseDto;
import org.bcp.dlmsenrolment.services.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * AdminRoleController - REST API endpoints for role management.
 * 
 * ENDPOINTS:
 * POST   /admin/roles                         - Create new role
 * GET    /admin/roles                         - List all roles
 * GET    /admin/roles/{id}                    - Get role by ID
 * PUT    /admin/roles/{id}                    - Update role
 * DELETE /admin/roles/{id}                    - Soft-delete role
 * POST   /admin/roles/{roleId}/permissions    - Assign permission to role
 * DELETE /admin/roles/{roleId}/permissions/{permissionId} - Remove permission from role
 * GET    /admin/roles/{roleId}/permissions    - Get role's permissions
 */
@RestController
@RequestMapping("/admin/roles")
@Tag(name = "Role Management", description = "REST APIs for role administration")
@RequiredArgsConstructor
@Slf4j
public class AdminRoleController {

    private final RoleService roleService;

    /**
     * POST /admin/roles - Create a new role
     */
    @PostMapping
    @Operation(summary = "Create a new role")
    public ResponseEntity<RoleResponseDto> createRole(
            @Valid @RequestBody RoleCreateRequestDto request) {
        log.info("Creating role: {}", request.getRoleName());
        RoleResponseDto response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /admin/roles - List all roles
     */
    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        log.info("Fetching all roles");
        List<RoleResponseDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /admin/roles/{id} - Get role by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable Long id) {
        log.info("Fetching role: {}", id);
        RoleResponseDto role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * PUT /admin/roles/{id} - Update role
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDto request) {
        log.info("Updating role: {}", id);
        RoleResponseDto response = roleService.updateRole(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /admin/roles/{id} - Soft-delete role
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.warn("Deleting role: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /admin/roles/{roleId}/permissions - Assign permission to role
     */
    @PostMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permission to role")
    public ResponseEntity<Void> assignPermissionToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionToRoleRequestDto request) {
        log.info("Assigning permission {} to role {}", request.getPermissionId(), roleId);
        roleService.assignPermissionToRole(roleId, request.getPermissionId());
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /admin/roles/{roleId}/permissions/{permissionId} - Remove permission from role
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from role")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /admin/roles/{roleId}/permissions - Get role's permissions
     */
    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Get role's assigned permissions")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable Long roleId) {
        log.info("Fetching permissions for role: {}", roleId);
        List<String> permissions = roleService.getRolePermissions(roleId);
        return ResponseEntity.ok(permissions);
    }
}
