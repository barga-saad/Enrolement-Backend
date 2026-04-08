package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * RolePermission Entity - Maps Roles to Permissions (M:N relationship).
 * 
 * This is a junction table that represents the assignment of permissions to roles.
 * 
 * Example:
 * - PROFIL_SAISIE role has permissions: PARTNER_CREATE, PARTNER_UPDATE_OWN, etc.
 * - PROFIL_VALIDATEUR role has different permissions
 * 
 * Data Structure:
 * - role_id (FK): Which role
 * - permission_id (FK): Which permission
 * 
 * Uniqueness: (role_id, permission_id) UNIQUE - A role can't have the same permission twice
 */
@Entity
@Table(
    name = "role_permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
    },
    indexes = {
        @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
        @Index(name = "idx_permission_id", columnList = "permission_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to Role table
     * When role is deleted, this entry remains (for audit) but role.deleted_at is set
     * However, we cascade delete when using softDelete on role
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_permission_role"))
    private Role role;

    /**
     * Foreign key to Permission table
     * When permission is deleted, this entry remains (for audit) but permission.deleted_at is set
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_permission_permission"))
    private Permission permission;

    /**
     * When this assignment was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", role=" + (role != null ? role.getRoleName() : "null") +
                ", permission=" + (permission != null ? permission.getPermissionName() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
