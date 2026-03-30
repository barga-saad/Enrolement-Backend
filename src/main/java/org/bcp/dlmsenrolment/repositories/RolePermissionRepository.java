package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RolePermissionRepository - Data access for RolePermission (M:N mapping).
 * 
 * Manages the relationships between roles and their permissions.
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * Find a role-permission mapping
     */
    @Query("""
        SELECT rp FROM RolePermission rp 
        WHERE rp.role.id = :roleId 
        AND rp.permission.id = :permissionId
        """)
    Optional<RolePermission> findByRoleAndPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * Find all permissions for a role
     */
    @Query("""
        SELECT rp FROM RolePermission rp 
        WHERE rp.role.id = :roleId
        ORDER BY rp.permission.permissionName ASC
        """)
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * Find all roles for a permission
     */
    @Query("""
        SELECT rp FROM RolePermission rp 
        WHERE rp.permission.id = :permissionId
        ORDER BY rp.role.roleName ASC
        """)
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * Check if role has a specific permission
     */
    @Query("""
        SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END 
        FROM RolePermission rp 
        WHERE rp.role.id = :roleId 
        AND rp.permission.id = :permissionId
        """)
    boolean roleHasPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
