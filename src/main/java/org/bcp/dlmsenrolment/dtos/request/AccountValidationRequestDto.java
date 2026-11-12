package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AccountValidationRequestDto(
        @NotBlank
        @Pattern(regexp = "\\d{24}")
        String accountNumber) {
}