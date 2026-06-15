package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PermissionCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.PermissionUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PermissionResponseDto;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.AuditService;
import org.bcp.dlmsenrolment.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * AdminPermissionController - REST API endpoints for permission management.
 */
@RestController
@RequestMapping("/admin/permissions")
@Tag(name = "Permission Management", description = "REST APIs for permission administration")
@RequiredArgsConstructor
@Slf4j
public class AdminPermissionController {

    private final PermissionService permissionService;
    private final AuditService auditService;

    @PostMapping
    @Operation(summary = "Create a new permission")
    public ResponseEntity<PermissionResponseDto> createPermission(
            @Valid @RequestBody PermissionCreateRequestDto request,
            Authentication authentication) {
        log.info("Creating permission: {}", request.getPermissionName());
        PermissionResponseDto response = permissionService.createPermission(request);
        String actor = SecurityUtils.getUsername(authentication);
        auditService.log(actor, "CREATE_PERMISSION", "PERMISSION", response.getId(),
                "Permission '" + response.getPermissionName() + "' created by " + actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<List<PermissionResponseDto>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<PermissionResponseDto> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    public ResponseEntity<PermissionResponseDto> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequestDto request,
            Authentication authentication) {
        log.info("Updating permission: {}", id);
        PermissionResponseDto response = permissionService.updatePermission(id, request);
        String actor = SecurityUtils.getUsername(authentication);
        auditService.log(actor, "UPDATE_PERMISSION", "PERMISSION", id,
                "Permission '" + response.getPermissionName() + "' updated by " + actor);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    public ResponseEntity<Void> deletePermission(
            @PathVariable Long id,
            Authentication authentication) {
        log.warn("Deleting permission: {}", id);
        permissionService.deletePermission(id);
        String actor = SecurityUtils.getUsername(authentication);
        auditService.log(actor, "DELETE_PERMISSION", "PERMISSION", id,
                "Permission " + id + " deleted by " + actor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner-check/true")
    @Operation(summary = "Get permissions requiring owner check")
    public ResponseEntity<List<PermissionResponseDto>> getPermissionsRequiringOwnerCheck() {
        return ResponseEntity.ok(permissionService.getPermissionsRequiringOwnerCheck());
    }
}