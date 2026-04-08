package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission Entity - Represents granular permissions in the system.
 * 
 * Permission Structure:
 * - permission_name: PARTNER_CREATE, PARTNER_UPDATE_OWN, etc.
 * - endpoint: /partners, /transfers, etc.
 * - http_method: GET, POST, PUT, DELETE, PATCH
 * - requires_owner_check: TRUE if permission requires owner validation
 *   (e.g., PARTNER_UPDATE_OWN - user can only update if they created it)
 * 
 * Soft-Delete Strategy:
 * - deleted_at: Set to current timestamp when permission is deleted
 * - When querying permissions, ALWAYS filter: WHERE deleted_at IS NULL
 * 
 * Relationships:
 * - Many-to-One with RolePermission: A permission can be assigned to multiple roles
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "permission_name"),
    @Index(name = "idx_endpoint_method", columnList = "endpoint, http_method"),
    @Index(name = "idx_permissions_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique permission identifier (e.g., "PARTNER_CREATE", "PARTNER_UPDATE_OWN")
     */
    @Column(name = "permission_name", nullable = false, unique = true, length = 100)
    private String permissionName;

    /**
     * API endpoint this permission grants access to (e.g., "/partners", "/transfers/{id}")
     */
    @Column(name = "endpoint", nullable = false, length = 255)
    private String endpoint;

    /**
     * HTTP method this permission applies to (GET, POST, PUT, DELETE, PATCH)
     */
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    /**
     * Human-readable description of what this permission does
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * FLAG: Does this permission require owner validation?
     * 
     * TRUE Examples:
     * - PARTNER_UPDATE_OWN: User can only update partners they created
     * - TRANSFER_VIEW_OWN: User can only view transfers they created
     * 
     * FALSE Examples:
     * - PARTNER_VIEW_ALL: User can view any partner
     * - TRANSFER_CREATE: User can create transfers
     * 
     * When TRUE, the Authorization Filter will check:
     * - Load the resource (partner, transfer, etc.)
     * - Check if resource.created_by == current_user.username
     * - Allow only if true, deny (403) if false
     */
    @Column(name = "requires_owner_check", nullable = false)
    @Builder.Default
    private Boolean requiresOwnerCheck = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ===== AUDIT FIELDS =====
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-Delete: Timestamp when permission was deleted
     * NULL = Active permission
     * NOT NULL = Deleted permission (but data remains in DB for audit trail)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ===== RELATIONSHIPS =====
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private Set<RolePermission> rolePermissions = new HashSet<>();

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.requiresOwnerCheck == null) {
            this.requiresOwnerCheck = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== HELPER METHODS =====

    /**
     * Check if this permission is currently active (not soft-deleted)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft-delete this permission
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * Restore a soft-deleted permission
     */
    public void restore() {
        this.deletedAt = null;
        this.isActive = true;
    }

    /**
     * Match this permission against requested endpoint and HTTP method
     * 
     * @param requestEndpoint The requested endpoint (e.g., "/partners")
     * @param requestMethod The requested HTTP method (e.g., "GET")
     * @return true if this permission matches the request
     */
    public boolean matches(String requestEndpoint, String requestMethod) {
        return this.endpoint.equalsIgnoreCase(requestEndpoint) &&
               this.httpMethod.equalsIgnoreCase(requestMethod) &&
               !this.isDeleted() &&
               this.isActive;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", permissionName='" + permissionName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", requiresOwnerCheck=" + requiresOwnerCheck +
                ", isActive=" + isActive +
                ", isDeleted=" + (deletedAt != null) +
                '}';
    }
}
