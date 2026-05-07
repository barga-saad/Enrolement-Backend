package org.bcp.dlmsenrolment.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.respons.PartnerValidationResponseDto;
import org.bcp.dlmsenrolment.entities.AuditLog;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.entities.PartnerValidation;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.excptions.PartnerNotFoundException;
import org.bcp.dlmsenrolment.repositories.AuditLogRepository;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.repositories.PartnerValidationRepository;
import org.bcp.dlmsenrolment.services.PartnerValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PartnerValidationServiceImpl implements PartnerValidationService {

    private final PartnerValidationRepository validationRepository;
    private final PartnerRepository partnerRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> submitForValidation(Long partnerId, String username) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("No partner found with id: " + partnerId));

        // Transition existing DRAFT validation to PENDING (avoid duplicate records)
        List<PartnerValidation> drafts = validationRepository.findByPartnerIdAndWorkflowStatus(partnerId, "DRAFT");
        PartnerValidation validation;
        if (!drafts.isEmpty()) {
            validation = drafts.get(0);
            validation.setWorkflowStatus("PENDING");
            validation.setCreatedBy(username);
        } else {
            validation = PartnerValidation.builder()
                    .partner(partner)
                    .workflowStatus("PENDING")
                    .actionType("CREATE")
                    .createdBy(username)
                    .build();
        }

        // Update partner status to PENDING
        partner.setWorkflowStatus("PENDING");
        partnerRepository.save(partner);

        PartnerValidation saved = validationRepository.save(validation);
        log.info("User {} submitted partner {} for validation", username, partnerId);

        logAudit(username, "SUBMIT_VALIDATION", "PARTNER_VALIDATION", saved.getId(),
                "Partner " + partner.getName() + " submitted for validation");

        return ResponseEntity.ok(ApiResponse.success("Partner submitted for validation.", toDto(saved)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> saveDraft(Long partnerId, String username) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("No partner found with id: " + partnerId));

        PartnerValidation validation = PartnerValidation.builder()
                .partner(partner)
                .workflowStatus("DRAFT")
                .actionType("CREATE")
                .createdBy(username)
                .build();

        PartnerValidation saved = validationRepository.save(validation);
        log.info("User {} saved draft for partner {}", username, partnerId);

        return ResponseEntity.ok(ApiResponse.success("Draft saved.", toDto(saved)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> validate(Long validationId, String validatorUsername) {
        PartnerValidation validation = validationRepository.findById(validationId)
                .orElseThrow(() -> new PartnerNotFoundException("No validation record found with id: " + validationId));

        if (!"PENDING".equals(validation.getWorkflowStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS", "Only PENDING validations can be validated.", "workflowStatus"));
        }

        if (validation.getCreatedBy().equals(validatorUsername)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("FOUR_EYES_VIOLATION",
                            "The validator cannot be the same person who submitted the request (4-eyes principle).",
                            "validatedBy"));
        }

        validation.setWorkflowStatus("VALIDATED");
        validation.setValidatedBy(validatorUsername);
        validation.setValidatedAt(LocalDateTime.now());

        PartnerValidation saved = validationRepository.save(validation);

        // Update the partner's own workflow status
        Partner partner = saved.getPartner();
        partner.setWorkflowStatus("VALIDATED");
        partnerRepository.save(partner);

        log.info("User {} validated partner validation {}", validatorUsername, validationId);

        logAudit(validatorUsername, "VALIDATE", "PARTNER_VALIDATION", saved.getId(),
                "Partner " + saved.getPartner().getName() + " validated");

        return ResponseEntity.ok(ApiResponse.success("Partner validated successfully.", toDto(saved)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> reject(Long validationId, String validatorUsername, String reason) {
        PartnerValidation validation = validationRepository.findById(validationId)
                .orElseThrow(() -> new PartnerNotFoundException("No validation record found with id: " + validationId));

        if (!"PENDING".equals(validation.getWorkflowStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS", "Only PENDING validations can be rejected.", "workflowStatus"));
        }

        if (validation.getCreatedBy().equals(validatorUsername)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("FOUR_EYES_VIOLATION",
                            "The rejector cannot be the same person who submitted the request (4-eyes principle).",
                            "validatedBy"));
        }

        validation.setWorkflowStatus("REJECTED");
        validation.setValidatedBy(validatorUsername);
        validation.setValidatedAt(LocalDateTime.now());
        validation.setRejectionReason(reason);

        PartnerValidation saved = validationRepository.save(validation);

        // Reset partner status to DRAFT so the saisie user can resubmit
        Partner partner = saved.getPartner();
        partner.setWorkflowStatus("DRAFT");
        partnerRepository.save(partner);

        log.info("User {} rejected partner validation {} - reason: {}", validatorUsername, validationId, reason);

        logAudit(validatorUsername, "REJECT", "PARTNER_VALIDATION", saved.getId(),
                "Partner " + saved.getPartner().getName() + " rejected: " + reason);

        return ResponseEntity.ok(ApiResponse.success("Partner rejected.", toDto(saved)));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getPending() {
        List<PartnerValidation> pending = validationRepository.findAllPending();
        List<PartnerValidationResponseDto> dtos = pending.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.success("Pending validations retrieved.", dtos));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getDrafts(String username) {
        List<PartnerValidation> drafts = validationRepository.findByCreatedByAndWorkflowStatus(username, "DRAFT");
        List<PartnerValidationResponseDto> dtos = drafts.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.success("Drafts retrieved.", dtos));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getHistory(Long partnerId) {
        List<PartnerValidation> history = validationRepository.findByPartnerIdOrderByCreatedAtDesc(partnerId);
        List<PartnerValidationResponseDto> dtos = history.stream().map(this::toDto).toList();
        return ResponseEntity.ok(ApiResponse.success("Validation history retrieved.", dtos));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> getValidation(Long validationId) {
        PartnerValidation validation = validationRepository.findById(validationId)
                .orElseThrow(() -> new PartnerNotFoundException("No validation record found with id: " + validationId));
        return ResponseEntity.ok(ApiResponse.success("Validation retrieved.", toDto(validation)));
    }

    // ===== HELPERS =====

    private PartnerValidationResponseDto toDto(PartnerValidation pv) {
        return PartnerValidationResponseDto.builder()
                .id(pv.getId())
                .partnerId(pv.getPartner().getId())
                .partnerName(pv.getPartner().getName())
                .partnerReferenceNumber(pv.getPartner().getPartnerReferenceNumber())
                .workflowStatus(pv.getWorkflowStatus())
                .actionType(pv.getActionType())
                .createdBy(pv.getCreatedBy())
                .validatedBy(pv.getValidatedBy())
                .validatedAt(pv.getValidatedAt())
                .rejectionReason(pv.getRejectionReason())
                .createdAt(pv.getCreatedAt())
                .updatedAt(pv.getUpdatedAt())
                .build();
    }

    private void logAudit(String username, String action, String entityType, Long entityId, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLogRepository.save(auditLog);
    }
}
