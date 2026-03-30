package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.AssignRoleToUserRequestDto;
import org.bcp.dlmsenrolment.dtos.request.UserCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.UserUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.UserResponseDto;
import org.bcp.dlmsenrolment.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * AdminUserController - REST API endpoints for user management.
 * 
 * ENDPOINTS:
 * POST   /admin/users                    - Create new user
 * GET    /admin/users                    - List all users
 * GET    /admin/users/{id}               - Get user by ID
 * PUT    /admin/users/{id}               - Update user
 * DELETE /admin/users/{id}               - Soft-delete user
 * POST   /admin/users/{userId}/roles     - Assign role to user
 * DELETE /admin/users/{userId}/roles/{roleId} - Revoke role from user
 * GET    /admin/users/{userId}/roles     - Get user's roles
 */
@RestController
@RequestMapping("/admin/users")
@Tag(name = "User Management", description = "REST APIs for user administration")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    /**
     * POST /admin/users - Create a new user
     */
    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto request,
            Authentication authentication) {
        log.info("Creating user: {}", request.getUsername());
        String createdByUsername = authentication.getName();
        UserResponseDto response = userService.createUser(request, createdByUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /admin/users - List all users
     */
    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Fetching all users");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /admin/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user: {}", id);
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /admin/users/{id} - Update user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto request) {
        log.info("Updating user: {}", id);
        UserResponseDto response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /admin/users/{id} - Soft-delete user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        log.warn("Deleting user: {}", id);
        String deletedByUsername = authentication.getName();
        userService.deleteUser(id, deletedByUsername);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /admin/users/{userId}/roles - Assign role to user
     */
    @PostMapping("/{userId}/roles")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleToUserRequestDto request,
            Authentication authentication) {
        log.info("Assigning role {} to user {}", request.getRoleId(), userId);
        String assignedByUsername = authentication.getName();
        userService.assignRoleToUser(userId, request.getRoleId(), assignedByUsername);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /admin/users/{userId}/roles/{roleId} - Revoke role from user
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @Operation(summary = "Revoke role from user")
    public ResponseEntity<Void> revokeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            Authentication authentication) {
        log.info("Revoking role {} from user {}", roleId, userId);
        String revokedByUsername = authentication.getName();
        userService.revokeRoleFromUser(userId, roleId, revokedByUsername);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /admin/users/{userId}/roles - Get user's roles
     */
    @GetMapping("/{userId}/roles")
    @Operation(summary = "Get user's assigned roles")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable Long userId) {
        log.info("Fetching roles for user: {}", userId);
        List<String> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }
}
