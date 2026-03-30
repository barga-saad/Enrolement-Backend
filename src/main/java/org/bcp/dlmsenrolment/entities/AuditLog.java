package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

/**
 * AuditLog Entity - Immutable audit trail of all sensitive operations.
 * 
 * IMPORTANT: This table is INSERT-ONLY. Never update, never delete.
 * 
 * Purpose: Track all sensitive operations for compliance and debugging
 * 
 * Logged Events:
 * - User login/logout
 * - Permission denied (403 Forbidden)
 * - Data access (GET, POST, PUT, DELETE)
 * - User management (create, update, delete user)
 * - Role management (create, update, delete role)
 * - Permission changes
 * - Failed authentication attempts
 * 
 * Immutability:
 * - No @PreUpdate, no @PrePersist modifying timestamps
 * - No updates allowed at database level (read-only view if needed)
 * - Only inserts allowed
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_entity_type", columnList = "entity_type"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_user_timestamp", columnList = "user_id, timestamp")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the user who performed the action
     * Can be NULL for anonymous actions or failures
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Username of the user who performed the action
     * Stored redundantly for quick queries
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * Type of action performed
     * Examples: LOGIN, LOGOUT, CREATE, UPDATE, DELETE, PERMISSION_DENIED, LOGIN_FAILED
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Type of entity affected
     * Examples: PARTNER, TRANSFER, USER, ROLE, PERMISSION
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the entity affected (if applicable)
     * For example: which partner was updated, which user was deleted
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Description of what happened
     * Examples: "User Ahmed deleted partner #42"
     *           "Validation failed for required field"
     *           "Permission denied: user lacks PARTNER_DELETE"
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Changes made (if applicable)
     * Can store JSON representation of what changed
     */
    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    /**
     * HTTP Status Code (for API operations)
     * Examples: 200, 201, 400, 403, 404, 500
     */
    @Column(name = "status_code")
    private Integer statusCode;

    /**
     * HTTP Method used (for API operations)
     * Examples: GET, POST, PUT, DELETE, PATCH
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /**
     * Request endpoint (for API operations)
     * Examples: /partners, /users/{id}
     */
    @Column(name = "endpoint", length = 255)
    private String endpoint;

    /**
     * IP address from which the request came
     * Useful for security analysis
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * When this action occurred
     * Set once at creation, never modified
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    // NO @PreUpdate - this entity is immutable

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", statusCode=" + statusCode +
                ", httpMethod='" + httpMethod + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
