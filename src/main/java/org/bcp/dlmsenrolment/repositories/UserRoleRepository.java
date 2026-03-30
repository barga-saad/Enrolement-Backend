package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * UserRoleRepository - Data access for UserRole (M:N mapping).
 * 
 * Manages the relationships between users and their roles.
 * Automatically filters soft-deleted assignments (deleted_at IS NULL)
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * Find all active role assignments for a user (NOT soft-deleted)
     * Critical for authorization filter - gets all roles user currently has
     */
    @Query("""
        SELECT ur FROM UserRole ur 
        WHERE ur.user.id = :userId 
        AND ur.deletedAt IS NULL
        ORDER BY ur.role.roleName ASC
        """)
    List<UserRole> findActiveRolesByUserId(@Param("userId") Long userId);

    /**
     * Find all active role assignments for a user by username (NOT soft-deleted)
     * Alternative method using username
     */
    @Query("""
        SELECT ur FROM UserRole ur 
        WHERE ur.user.username = :username 
        AND ur.deletedAt IS NULL
        ORDER BY ur.role.roleName ASC
        """)
    List<UserRole> findActiveRolesByUsername(@Param("username") String username);

    /**
     * Find all users with a specific role (active assignments only)
     */
    @Query("""
        SELECT ur FROM UserRole ur 
        WHERE ur.role.id = :roleId 
        AND ur.deletedAt IS NULL
        ORDER BY ur.user.username ASC
        """)
    List<UserRole> findUsersByRoleId(@Param("roleId") Long roleId);

    /**
     * Check if user has a specific role (active assignment only)
     */
    @Query("""
        SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END 
        FROM UserRole ur 
        WHERE ur.user.id = :userId 
        AND ur.role.id = :roleId
        AND ur.deletedAt IS NULL
        """)
    boolean userHasRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * Check if user has a specific role by username
     */
    @Query("""
        SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END 
        FROM UserRole ur 
        WHERE ur.user.username = :username 
        AND ur.role.roleName = :roleName
        AND ur.deletedAt IS NULL
        """)
    boolean userHasRoleByUsername(@Param("username") String username, @Param("roleName") String roleName);

    /**
     * Get all role IDs for a user (returns Set for efficient operations)
     * Used in PermissionResolver
     */
    @Query("""
        SELECT ur.role.id FROM UserRole ur 
        WHERE ur.user.id = :userId 
        AND ur.deletedAt IS NULL
        """)
    Set<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * Find revoked role assignments (for audit purposes)
     */
    @Query("""
        SELECT ur FROM UserRole ur 
        WHERE ur.user.id = :userId 
        AND ur.deletedAt IS NOT NULL
        ORDER BY ur.deletedAt DESC
        """)
    List<UserRole> findRevokedRolesByUserId(@Param("userId") Long userId);

    /**
     * Find a specific user-role assignment
     */
    @Query("""
        SELECT ur FROM UserRole ur 
        WHERE ur.user.id = :userId 
        AND ur.role.id = :roleId
        """)
    Optional<UserRole> findByUserAndRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
