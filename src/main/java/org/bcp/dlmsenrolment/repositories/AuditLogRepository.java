package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuditLogRepository - Data access for AuditLog (immutable).
 * 
 * IMPORTANT: This repository is INSERT-ONLY.
 * No updates, no deletes. Read-only queries only.
 * 
 * Supports compliance, debugging, and security analysis.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.userId = :userId 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    /**
     * Find all audit logs for a specific user (paginated)
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.userId = :userId 
        ORDER BY al.timestamp DESC
        """)
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find all audit logs of a specific action type
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.action = :action 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findByAction(@Param("action") String action);

    /**
     * Find all audit logs for a specific entity
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.entityType = :entityType 
        AND al.entityId = :entityId 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * Find all permission denied events
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.action = 'PERMISSION_DENIED' 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findPermissionDeniedEvents();

    /**
     * Find all failed login attempts
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.action = 'LOGIN_FAILED' 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findFailedLogins();

    /**
     * Find all failed logins for a specific username
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.action = 'LOGIN_FAILED' 
        AND al.username = :username 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findFailedLoginsByUsername(@Param("username") String username);

    /**
     * Find all login attempts from a specific IP address
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.action IN ('LOGIN', 'LOGIN_FAILED') 
        AND al.ipAddress = :ipAddress 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findLoginAttemptsByIp(@Param("ipAddress") String ipAddress);

    /**
     * Find audit logs in a time range
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.timestamp >= :startTime 
        AND al.timestamp <= :endTime 
        ORDER BY al.timestamp DESC
        """)
    List<AuditLog> findByTimestampRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Find audit logs by entity type (paginated)
     */
    @Query("""
        SELECT al FROM AuditLog al 
        WHERE al.entityType = :entityType 
        ORDER BY al.timestamp DESC
        """)
    Page<AuditLog> findByEntityType(@Param("entityType") String entityType, Pageable pageable);

    /**
     * Count failed logins for a username (for brute-force detection)
     */
    @Query("""
        SELECT COUNT(al) FROM AuditLog al 
        WHERE al.action = 'LOGIN_FAILED' 
        AND al.username = :username 
        AND al.timestamp >= :since
        """)
    long countFailedLoginsSince(@Param("username") String username, @Param("since") LocalDateTime since);
}
