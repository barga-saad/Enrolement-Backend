package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.PartnerValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerValidationRepository extends JpaRepository<PartnerValidation, Long> {

    List<PartnerValidation> findByWorkflowStatus(String workflowStatus);

    List<PartnerValidation> findByPartnerIdAndWorkflowStatus(Long partnerId, String workflowStatus);

    @Query("SELECT pv FROM PartnerValidation pv WHERE pv.partner.id = :partnerId ORDER BY pv.createdAt DESC")
    List<PartnerValidation> findByPartnerIdOrderByCreatedAtDesc(@Param("partnerId") Long partnerId);

    Optional<PartnerValidation> findTopByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    List<PartnerValidation> findByCreatedByAndWorkflowStatus(String createdBy, String workflowStatus);

    @Query("SELECT pv FROM PartnerValidation pv WHERE pv.workflowStatus = 'PENDING' ORDER BY pv.createdAt DESC")
    List<PartnerValidation> findAllPending();
}
