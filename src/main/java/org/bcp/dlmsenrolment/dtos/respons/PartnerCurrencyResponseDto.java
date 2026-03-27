package org.bcp.dlmsenrolment.dtos.respons;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PartnerCurrencyResponseDto {
////    @NotBlank
//    private String currency;
    @NotEmpty(message = "Currencies list cannot be empty.")
    private Set<@Size(min = 3, max = 3, message = "Currency code must be 3 characters long.") String> currencies;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String partnerReferenceNumber;
}

