package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * PermissionRepository - Data access for Permission entity.
 * 
 * All queries automatically filter soft-deleted permissions (deleted_at IS NULL)
 * unless specifically querying deleted permissions.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Find active permission by name (NOT soft-deleted)
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionName = :permissionName AND p.deletedAt IS NULL")
    Optional<Permission> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Find all active permissions (NOT soft-deleted)
     */
    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL ORDER BY p.permissionName ASC")
    List<Permission> findAllActive();

    /**
     * Find permissions by endpoint and HTTP method
     * Used during authorization filter to match incoming requests
     */
    @Query("""
        SELECT p FROM Permission p 
        WHERE p.endpoint = :endpoint 
        AND p.httpMethod = :httpMethod 
        AND p.deletedAt IS NULL 
        AND p.isActive = true
        """)
    List<Permission> findByEndpointAndMethod(@Param("endpoint") String endpoint, @Param("httpMethod") String httpMethod);

    /**
     * Find all permissions for a specific role (NOT soft-deleted)
     * Critical for authorization filter
     */
    @Query("""
        SELECT DISTINCT p FROM Permission p
        JOIN p.rolePermissions rp
        WHERE rp.role.id = :roleId
        AND p.deletedAt IS NULL
        AND p.isActive = true
        """)
    Set<Permission> findPermissionsByRole(@Param("roleId") Long roleId);

    /**
     * Find all permissions for multiple roles (for multi-role users)
     * Returns UNION of all permissions from all specified roles
     */
    @Query("""
        SELECT DISTINCT p FROM Permission p
        JOIN p.rolePermissions rp
        WHERE rp.role.id IN :roleIds
        AND p.deletedAt IS NULL
        AND p.isActive = true
        """)
    Set<Permission> findPermissionsByRoles(@Param("roleIds") List<Long> roleIds);

    /**
     * Find permissions that require owner check
     * Useful for identifying sensitive operations
     */
    @Query("""
        SELECT p FROM Permission p 
        WHERE p.requiresOwnerCheck = true 
        AND p.deletedAt IS NULL 
        AND p.isActive = true
        ORDER BY p.permissionName ASC
        """)
    List<Permission> findPermissionsRequiringOwnerCheck();

    /**
     * Check if permission exists by name (active only)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.permissionName = :permissionName AND p.deletedAt IS NULL")
    boolean existsByPermissionNameActive(@Param("permissionName") String permissionName);

    /**
     * Find deleted permissions (for admin recovery)
     */
    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NOT NULL ORDER BY p.deletedAt DESC")
    List<Permission> findDeletedPermissions();
}
