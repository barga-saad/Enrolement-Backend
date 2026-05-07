package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.RejectRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerValidationResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.PartnerValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/validations")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Partner Validation", description = "Workflow validation endpoints (4-eyes principle)")
public class ValidationController {

    private final PartnerValidationService validationService;

    @PostMapping("/partners/{partnerId}/submit")
    @Operation(summary = "Submit partner for validation", description = "Requires PARTNER_CREATE or PARTNER_SUBMIT permission")
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> submitForValidation(
            @PathVariable Long partnerId,
            Authentication authentication) {
        String username = SecurityUtils.getUsername(authentication);
        log.info("User {} submitting partner {} for validation", username, partnerId);
        return validationService.submitForValidation(partnerId, username);
    }

    @PostMapping("/partners/{partnerId}/draft")
    @Operation(summary = "Save partner as draft", description = "Requires PARTNER_CREATE permission")
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> saveDraft(
            @PathVariable Long partnerId,
            Authentication authentication) {
        String username = SecurityUtils.getUsername(authentication);
        log.info("User {} saving draft for partner {}", username, partnerId);
        return validationService.saveDraft(partnerId, username);
    }

    @PostMapping("/{validationId}/validate")
    @Operation(summary = "Validate a pending partner", description = "Requires PARTNER_VALIDATE permission. 4-eyes: validator != submitter")
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> validate(
            @PathVariable Long validationId,
            Authentication authentication) {
        String username = SecurityUtils.getUsername(authentication);
        log.info("User {} validating validation record {}", username, validationId);
        return validationService.validate(validationId, username);
    }

    @PostMapping("/{validationId}/reject")
    @Operation(summary = "Reject a pending partner", description = "Requires PARTNER_VALIDATE permission. 4-eyes: rejector != submitter")
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> reject(
            @PathVariable Long validationId,
            @RequestBody RejectRequestDto request,
            Authentication authentication) {
        String username = SecurityUtils.getUsername(authentication);
        log.info("User {} rejecting validation record {} - reason: {}", username, validationId, request.getReason());
        return validationService.reject(validationId, username, request.getReason());
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending validations", description = "Requires PARTNER_VALIDATE permission")
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getPending() {
        log.info("Request to fetch all pending validations");
        return validationService.getPending();
    }

    @GetMapping("/drafts")
    @Operation(summary = "Get my drafts", description = "Returns drafts of the current user")
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getDrafts(
            Authentication authentication) {
        String username = SecurityUtils.getUsername(authentication);
        log.info("User {} fetching drafts", username);
        return validationService.getDrafts(username);
    }

    @GetMapping("/history/{partnerId}")
    @Operation(summary = "Get validation history for a partner")
    public ResponseEntity<ApiResponse<List<PartnerValidationResponseDto>>> getHistory(
            @PathVariable Long partnerId) {
        log.info("Request to fetch validation history for partner {}", partnerId);
        return validationService.getHistory(partnerId);
    }

    @GetMapping("/{validationId}")
    @Operation(summary = "Get a specific validation record")
    public ResponseEntity<ApiResponse<PartnerValidationResponseDto>> getValidation(
            @PathVariable Long validationId) {
        log.info("Request to fetch validation record {}", validationId);
        return validationService.getValidation(validationId);
    }
}
