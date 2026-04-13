package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "partner_validation", indexes = {
    @Index(name = "idx_pv_workflow_status", columnList = "workflow_status"),
    @Index(name = "idx_pv_created_by", columnList = "created_by"),
    @Index(name = "idx_pv_partner_id", columnList = "partner_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "workflow_status", nullable = false, length = 20)
    private String workflowStatus; // DRAFT, PENDING, VALIDATED, REJECTED

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // CREATE, UPDATE, DEACTIVATE

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
