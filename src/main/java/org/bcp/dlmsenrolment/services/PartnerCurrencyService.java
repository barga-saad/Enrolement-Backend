package org.bcp.dlmsenrolment.services;

import org.bcp.dlmsenrolment.dtos.request.PartnerCurrencyRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerCurrencyResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.springframework.http.ResponseEntity;


public interface PartnerCurrencyService {
    ResponseEntity<ApiResponse<PartnerCurrencyRequestDto>> getPartnerCurrencies(String referenceNumber);
    ResponseEntity<ApiResponse<PartnerCurrencyResponseDto>> createPartnerCurrency(PartnerCurrencyRequestDto partnerCurrency);
    ResponseEntity<ApiResponse<PartnerCurrencyResponseDto>> updatePartnerCurrencies(PartnerCurrencyRequestDto partnerCurrency);
    ResponseEntity<ApiResponse<PartnerCurrencyResponseDto>> deletePartnerCurrencies(String referenceNumber);
}
