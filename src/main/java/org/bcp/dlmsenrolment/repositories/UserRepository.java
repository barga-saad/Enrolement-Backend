package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Data access for User entity.
 * 
 * All queries automatically filter soft-deleted users (deleted_at IS NULL)
 * unless specifically querying deleted users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find active user by username (NOT soft-deleted)
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Find active user by email (NOT soft-deleted)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Find all active users (NOT soft-deleted)
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.username ASC")
    List<User> findAllActive();

    /**
     * Find all active users by specific role
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        WHERE r.id = :roleId 
        AND u.deletedAt IS NULL 
        AND ur.deletedAt IS NULL
        ORDER BY u.username ASC
        """)
    List<User> findActiveUsersByRole(@Param("roleId") Long roleId);

    /**
     * Find deleted users (for admin recovery)
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL ORDER BY u.deletedAt DESC")
    List<User> findDeletedUsers();

    /**
     * Check if user exists by username (active only)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsernameActive(@Param("username") String username);
}
