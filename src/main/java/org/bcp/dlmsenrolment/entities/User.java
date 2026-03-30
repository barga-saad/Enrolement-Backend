package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Represents authenticated users in the system.
 * 
 * Soft-Delete Strategy:
 * - deleted_at: Set to current timestamp when user is deleted
 * - deleted_by: Username of admin who deleted this user
 * - When querying users, ALWAYS filter: WHERE deleted_at IS NULL
 * 
 * Relationships:
 * - One-to-Many with UserRole: A user can have multiple roles
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ===== AUDIT FIELDS =====
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft-Delete: Timestamp when user was deleted
     * NULL = Active user
     * NOT NULL = Deleted user (but data remains in DB for audit trail)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft-Delete: Username of the admin who deleted this user
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // ===== RELATIONSHIPS =====
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
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
     * Check if this user is currently active (not soft-deleted)
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft-delete this user
     */
    public void softDelete(String deletedByUsername) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedByUsername;
        this.isActive = false;
    }

    /**
     * Restore a soft-deleted user
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.isActive = true;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + (deletedAt != null) +
                '}';
    }
}
