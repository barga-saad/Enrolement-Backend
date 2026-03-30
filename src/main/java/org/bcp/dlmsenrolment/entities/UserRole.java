package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * UserRole Entity - Maps Users to Roles (M:N relationship).
 * 
 * This is a junction table that represents the assignment of roles to users.
 * A user can have MULTIPLE roles simultaneously.
 * 
 * Example:
 * - Ahmed Benali has roles: [PROFIL_SAISIE, PROFIL_VALIDATEUR, PROFIL_CONSULTATION]
 * - When requesting an endpoint, user gets UNION of all permissions from all roles
 * 
 * Data Structure:
 * - user_id (FK): Which user
 * - role_id (FK): Which role
 * - assigned_at: When this role was assigned
 * - assigned_by: Username of admin who assigned this role
 * 
 * Uniqueness: (user_id, role_id) UNIQUE - A user can't have the same role twice
 * 
 * Soft-Delete Strategy:
 * - deleted_at: Set when role assignment is revoked
 * - When querying user roles, filter: WHERE deleted_at IS NULL
 */
@Entity
@Table(
    name = "user_roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_role_id", columnList = "role_id"),
        @Index(name = "idx_deleted_at", columnList = "deleted_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to User table
     * This user has this role
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_user"))
    private User user;

    /**
     * Foreign key to Role table
     * The role assigned to this user
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role_role"))
    private Role role;

    /**
     * When this role was assigned to the user
     */
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    /**
     * Username of the admin who assigned this role
     */
    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    /**
     * Soft-Delete: Timestamp when this role assignment was revoked
     * NULL = Active assignment (user currently has this role)
     * NOT NULL = Revoked assignment (data remains for audit trail)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Username of the admin who revoked this role
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    protected void onCreate() {
        if (this.assignedAt == null) {
            this.assignedAt = LocalDateTime.now();
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Check if this role assignment is currently active (not soft-deleted)
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    /**
     * Revoke this role assignment (soft-delete)
     */
    public void revoke(String revokedByUsername) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = revokedByUsername;
    }

    /**
     * Restore this role assignment
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", role=" + (role != null ? role.getRoleName() : "null") +
                ", assignedAt=" + assignedAt +
                ", assignedBy='" + assignedBy + '\'' +
                ", isActive=" + (deletedAt == null) +
                '}';
    }
}
