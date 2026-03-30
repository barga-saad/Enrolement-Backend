package org.bcp.dlmsenrolment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.entities.User;
import org.bcp.dlmsenrolment.entities.UserRole;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.bcp.dlmsenrolment.repositories.UserRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PermissionResolver Service - Core RBAC Permission Resolution Logic.
 * 
 * CRITICAL: This service is called on EVERY REQUEST to:
 * 1. Load user from database
 * 2. Get all user's roles
 * 3. Get all permissions for all roles
 * 4. Provide method to check if user has specific permission
 * 5. Provide method to check if permission requires owner check
 * 
 * Performance Note:
 * - Repositories use custom @Query with projections
 * - Filters are applied at database level (WHERE deleted_at IS NULL)
 * - Results are cached in SecurityContext for the request
 * 
 * ARCHITECTURAL DECISION:
 * - Load permissions DYNAMICALLY from DB on each request
 * - NOT using Spring Security's default role/authority system
 * - Custom authorization filter uses this service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionResolver {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Get all permissions for a user
     * 
     * Algorithm:
     * 1. Get user from database
     * 2. Get all user's roles (WHERE deleted_at IS NULL)
     * 3. For each role, get all permissions
     * 4. Collect into Set (duplicate removal automatic)
     * 5. Return Union of all permissions
     * 
     * @param username The username
     * @return Set of all permissions this user has (from all roles)
     */
    public Set<Permission> getUserPermissions(String username) {
        log.debug("Loading permissions for user: {}", username);

        // Step 1: Load user from database
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            return Set.of(); // Empty set - user has no permissions
        }

        User user = userOpt.get();
        return getUserPermissions(user.getId());
    }

    /**
     * Get all permissions for a user by user ID
     * 
     * @param userId The user ID
     * @return Set of all permissions this user has
     */
    public Set<Permission> getUserPermissions(Long userId) {
        log.debug("Loading permissions for user ID: {}", userId);

        // Step 1: Get all user's role IDs (active roles only)
        Set<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            log.warn("User ID {} has no active roles", userId);
            return Set.of(); // User has no roles → no permissions
        }

        log.debug("User {} has {} roles", userId, roleIds.size());

        // Step 2: Get all permissions for all these roles (UNION)
        Set<Permission> permissions = permissionRepository.findPermissionsByRoles(
            new ArrayList<>(roleIds)
        );

        log.debug("User {} has {} total permissions from {} roles", 
            userId, permissions.size(), roleIds.size());

        return permissions;
    }

    /**
     * Check if user has a specific permission
     * 
     * @param username The username
     * @param permissionName The permission to check (e.g., "PARTNER_CREATE")
     * @return true if user has this permission, false otherwise
     */
    public boolean hasPermission(String username, String permissionName) {
        Set<Permission> permissions = getUserPermissions(username);
        return permissions.stream()
            .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
    }

    /**
     * Check if user has a specific permission by user ID
     */
    public boolean hasPermission(Long userId, String permissionName) {
        Set<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
            .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
    }

    /**
     * Find all permissions that match a request endpoint and HTTP method
     * 
     * Called by Authorization Filter to match incoming request to permissions
     * 
     * @param endpoint The endpoint (e.g., "/partners")
     * @param httpMethod The HTTP method (e.g., "GET")
     * @return List of matching permissions
     */
    public List<Permission> findMatchingPermissions(String endpoint, String httpMethod) {
        return permissionRepository.findByEndpointAndMethod(endpoint, httpMethod);
    }

    /**
     * Check if a permission requires owner validation
     * 
     * Used in Authorization Filter:
     * - If requires_owner_check = TRUE
     * - Load the resource (partner, transfer, etc.)
     * - Check if resource.created_by == current_user
     * - Allow only if match
     * 
     * @param permissionName The permission name
     * @return true if this permission needs owner check
     */
    public boolean requiresOwnerCheck(String permissionName) {
        Optional<Permission> permOpt = permissionRepository.findByPermissionName(permissionName);
        return permOpt.map(Permission::getRequiresOwnerCheck).orElse(false);
    }

    /**
     * Get all active roles for a user
     * 
     * @param username The username
     * @return List of Role objects
     */
    public List<String> getUserRoleNames(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return List.of();
        }

        return userRoleRepository.findActiveRolesByUserId(userOpt.get().getId())
            .stream()
            .map(UserRole::getRole)
            .map(role -> role.getRoleName())
            .collect(Collectors.toList());
    }

    /**
     * Check if user has a specific role
     * 
     * @param username The username
     * @param roleName The role name to check
     * @return true if user has this role
     */
    public boolean hasRole(String username, String roleName) {
        return userRoleRepository.userHasRoleByUsername(username, roleName);
    }

    /**
     * Get a specific permission by name
     * 
     * @param permissionName The permission name
     * @return Optional containing the permission if found
     */
    public Optional<Permission> getPermission(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName);
    }

    /**
     * Check if user can access a specific endpoint with specific method
     * 
     * This is a convenience method for authorization checks
     * 
     * @param username The username
     * @param endpoint The endpoint
     * @param httpMethod The HTTP method
     * @return true if user has permission
     */
    public boolean canAccess(String username, String endpoint, String httpMethod) {
        Set<Permission> userPermissions = getUserPermissions(username);
        return userPermissions.stream()
            .anyMatch(p -> p.matches(endpoint, httpMethod));
    }

    /**
     * Log permission checking details (for debugging)
     * 
     * @param username The username
     * @param endpoint The endpoint requested
     * @param httpMethod The HTTP method
     * @param allowed Whether access was allowed
     */
    public void logPermissionCheck(String username, String endpoint, String httpMethod, boolean allowed) {
        if (allowed) {
            log.debug("PERMISSION GRANTED - User: {}, Endpoint: {} {}", username, httpMethod, endpoint);
        } else {
            log.warn("PERMISSION DENIED - User: {}, Endpoint: {} {}", username, httpMethod, endpoint);
        }
    }
}
