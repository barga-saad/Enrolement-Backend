package org.bcp.dlmsenrolment.services;

import org.bcp.dlmsenrolment.dtos.respons.PartnerValidationResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PartnerValidationService {

    ResponseEntity<ApiResponse<PartnerValidationResponseDto>> submitForValidation(Long partnerId, String username);

    ResponseEntity<ApiResponse<PartnerValidationResponseDto>> saveDraft(Long partnerId, String username);

    ResponseEntity<ApiResponse<PartnerValidationResponseDto>> validate(Long validationId, String validatorUsername);

    ResponseEntity<ApiResponse<PartnerValidationResponseDto>> reject(Long validationId, String validatorUsername, String reason);

    ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getPending();

    ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getDrafts(String username);

    ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getHistory(Long partnerId);

    ResponseEntity<ApiResponse<PartnerValidationResponseDto>> getValidation(Long validationId);
}
