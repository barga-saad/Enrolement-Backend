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
 * Role Entity - Represents roles/profiles in the system.
 * 
 * Predefined Roles:
 * - PROFIL_SAISIE: Data entry user
 * - PROFIL_VALIDATEUR: Validator/reviewer
 * - PROFIL_CONSULTATION: View-only consultant
 * - PROFIL_ADMIN_CENTRAL: System administrator
 * 
 * Soft-Delete Strategy:
 * - deleted_at: Set to current timestamp when role is deleted
 * - When querying roles, ALWAYS filter: WHERE deleted_at IS NULL
 * 
 * Relationships:
 * - Many-to-One with RolePermission: A role has multiple permissions
 * - Many-to-One with UserRole: A role is assigned to multiple users
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "role_name"),
    @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    private String roleName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ===== AUDIT FIELDS =====
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-Delete: Timestamp when role was deleted
     * NULL = Active role
     * NOT NULL = Deleted role (but data remains in DB for audit trail)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ===== RELATIONSHIPS =====
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== HELPER METHODS =====

    /**
     * Check if this role is currently active (not soft-deleted)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft-delete this role
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }

    /**
     * Restore a soft-deleted role
     */
    public void restore() {
        this.deletedAt = null;
        this.isActive = true;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + (deletedAt != null) +
                '}';
    }
}
