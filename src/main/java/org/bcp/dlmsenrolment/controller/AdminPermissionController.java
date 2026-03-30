package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PermissionCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.PermissionUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PermissionResponseDto;
import org.bcp.dlmsenrolment.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * AdminPermissionController - REST API endpoints for permission management.
 * 
 * ENDPOINTS:
 * POST   /admin/permissions              - Create new permission
 * GET    /admin/permissions              - List all permissions
 * GET    /admin/permissions/{id}         - Get permission by ID
 * PUT    /admin/permissions/{id}         - Update permission
 * DELETE /admin/permissions/{id}         - Soft-delete permission
 * GET    /admin/permissions/owner-check  - Get permissions requiring owner check
 */
@RestController
@RequestMapping("/admin/permissions")
@Tag(name = "Permission Management", description = "REST APIs for permission administration")
@RequiredArgsConstructor
@Slf4j
public class AdminPermissionController {

    private final PermissionService permissionService;

    /**
     * POST /admin/permissions - Create a new permission
     */
    @PostMapping
    @Operation(summary = "Create a new permission")
    public ResponseEntity<PermissionResponseDto> createPermission(
            @Valid @RequestBody PermissionCreateRequestDto request) {
        log.info("Creating permission: {}", request.getPermissionName());
        PermissionResponseDto response = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /admin/permissions - List all permissions
     */
    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<List<PermissionResponseDto>> getAllPermissions() {
        log.info("Fetching all permissions");
        List<PermissionResponseDto> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * GET /admin/permissions/{id} - Get permission by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<PermissionResponseDto> getPermissionById(@PathVariable Long id) {
        log.info("Fetching permission: {}", id);
        PermissionResponseDto permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    /**
     * PUT /admin/permissions/{id} - Update permission
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    public ResponseEntity<PermissionResponseDto> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequestDto request) {
        log.info("Updating permission: {}", id);
        PermissionResponseDto response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /admin/permissions/{id} - Soft-delete permission
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        log.warn("Deleting permission: {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /admin/permissions/owner-check - Get permissions requiring owner check
     */
    @GetMapping("/owner-check/true")
    @Operation(summary = "Get permissions requiring owner check")
    public ResponseEntity<List<PermissionResponseDto>> getPermissionsRequiringOwnerCheck() {
        log.info("Fetching permissions requiring owner check");
        List<PermissionResponseDto> permissions = permissionService.getPermissionsRequiringOwnerCheck();
        return ResponseEntity.ok(permissions);
    }
}
