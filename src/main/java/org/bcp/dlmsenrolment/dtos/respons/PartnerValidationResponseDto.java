package org.bcp.dlmsenrolment.dtos.respons;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PartnerValidationResponseDto {
    private Long id;
    private Long partnerId;
    private String partnerName;
    private String partnerReferenceNumber;
    private String workflowStatus;
    private String actionType;
    private String createdBy;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
