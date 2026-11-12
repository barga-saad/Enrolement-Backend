package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoleRepository - Data access for Role entity.
 * 
 * All queries automatically filter soft-deleted roles (deleted_at IS NULL)
 * unless specifically querying deleted roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find active role by name (NOT soft-deleted)
     */
    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName AND r.deletedAt IS NULL")
    Optional<Role> findByRoleName(@Param("roleName") String roleName);

    /**
     * Find all active roles (NOT soft-deleted)
     */
    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL ORDER BY r.roleName ASC")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role r ORDER BY r.roleName ASC")
    List<Role> findAllForAdmin();

    /**
     * Check if role exists by name (active only)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.roleName = :roleName")
    boolean existsByRoleName(@Param("roleName") String roleName);

    /**
     * Find deleted roles (for admin recovery)
     */
    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NOT NULL ORDER BY r.deletedAt DESC")
    List<Role> findDeletedRoles();
}
