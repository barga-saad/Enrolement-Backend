package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PartnerStatusUpdateRequestDto {

    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String partnerStatus;
}