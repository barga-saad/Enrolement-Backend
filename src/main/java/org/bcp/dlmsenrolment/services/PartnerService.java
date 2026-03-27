package org.bcp.dlmsenrolment.services;

import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PartnerService {
    ResponseEntity<ApiResponse<PartnerResponseDto>> getPartner(String referenceNumber);
    ResponseEntity<ApiResponse<List<PartnerRequestDto>>> getPartners();
    ResponseEntity<ApiResponse<PartnerResponseDto>> createPartner(PartnerRequestDto partnerRequestDto);
    ResponseEntity<ApiResponse<PartnerResponseDto>> updatePartner(PartnerRequestDto partnerRequestDto);
    ResponseEntity<ApiResponse<PartnerResponseDto>> deletePartner(String referenceNumber);
}
