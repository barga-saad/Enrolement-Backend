package org.bcp.dlmsenrolment.dtos.respons;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class PartnerResponseDto {
    // You can uncomment id if you want it in responses/updates
    // private Long id;

    private String name;
    private String partnerReferenceNumber;
    private String partnerType;
    private boolean partnerStatus;
}
