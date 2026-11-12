package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.request.PartnerStatusUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.AuditService;
import org.bcp.dlmsenrolment.services.PartnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * PartnerController - REST API endpoints for partner management.
 * 
 * SECURITY MODEL:
 * - All endpoints require valid JWT token from Keycloak
 * - Permissions enforced by PermissionAuthorizationFilter:
 *   - GET /partners/{id} requires PARTNER_VIEW_OWN (owner-check) or PARTNER_VIEW_ALL
 * 
 * OWNER-CHECK ENFORCEMENT:
 * - PARTNER_VIEW_OWN: User can only view partners they created
 * - Enforced automatically by PermissionAuthorizationFilter
 * 
 * ROLES:
 * - PROFIL_SAISIE: Can create and update partner records
 * - PROFIL_VALIDATEUR: Can validate partner records
 * - PROFIL_CONSULTATION: Can view partner records (read-only)
 * - PROFIL_ADMIN_CENTRAL: Full access to all records
 * 
 */
@RestController
@Slf4j
@Tag(name = "Partner Management", description = "Partner endpoints with RBAC")
public class PartnerController {
    private final PartnerService partnerService;
    private final AuditService auditService;

    public PartnerController(PartnerService partnerService, AuditService auditService) {
        this.partnerService = partnerService;
        this.auditService = auditService;
    }

    /**
     * GET /v1/read/partners - Get all partners
     * <p>
     * SECURITY:
     * - Requires PARTNER_LIST_ALL permission
     * - All authenticated users with appropriate role can access
     *
     * @return List of all partners
     */
    @GetMapping("/v1/read/partners")
    @Operation(summary = "Get all partners", 
               description = "Requires PARTNER_LIST_ALL permission")
    public ResponseEntity<?> getAllPartners() {
        log.info("Request to fetch all partners");
        return partnerService.getPartners();
    }

    /**
     * GET /getpartner/{partnerId} - Get partner by ID
     * <p>
     * SECURITY:
     * - Requires PARTNER_VIEW_OWN (with owner-check) OR PARTNER_VIEW_ALL permission
     * - If user has PARTNER_VIEW_OWN: Must be the creator of the partner
     * - If user has PARTNER_VIEW_ALL: Can view any partner
     *
     * @param partnerId      - Partner identifier
     * @param authentication - Current authenticated user (from JWT token)
     * @return Partner data
     */
    @GetMapping("getpartner/{partnerId}")
    @Operation(summary = "Get partner by ID", 
               description = "Requires PARTNER_VIEW_OWN (own records) or PARTNER_VIEW_ALL (all records)")
    public ResponseEntity<ApiResponse<PartnerResponseDto>> getPartner(
            @PathVariable String partnerId,
            Authentication authentication) {
        log.info("User {} requested partner: {}", SecurityUtils.getUsername(authentication), partnerId);
        return partnerService.getPartner(partnerId);
    }

    /**
     * POST /v1/create/partners - Create a new partner
     * <p>
     * SECURITY:
     * - Requires PARTNER_CREATE permission
     * - Creator's username will be recorded in the partner record
     *
     * @param request        - Partner creation request
     * @param authentication - Current authenticated user
     * @return Created partner
     */
    @PostMapping("/v1/create/partners")
    @Operation(summary = "Create a new partner")
    public ResponseEntity<ApiResponse<PartnerResponseDto>> createPartner(
            @Valid @RequestBody PartnerRequestDto request,
            Authentication authentication) {
        log.info("User {} creating new partner: {}", SecurityUtils.getUsername(authentication), request.getName());
        ResponseEntity<ApiResponse<PartnerResponseDto>> response = partnerService.createPartner(request);
        String actor = SecurityUtils.getUsername(authentication);
        if (response.getBody() != null && response.getBody().getData() != null) {
            auditService.log(actor, "CREATE_PARTNER", "PARTNER",
                    response.getBody().getData().getId(),
                    "Partner created: " + request.getName() + " (ref: " + request.getPartnerReferenceNumber() + ")");
        }
        return response;
    }

    /**
     * PUT /v1/update/partners - Update an existing partner
     * <p>
     * SECURITY:
     * - Requires PARTNER_UPDATE_OWN (own) or admin permission
     *
     * @param request - Partner update request
     * @return Updated partner
     */
    @PutMapping("/v1/update/partners")
    @Operation(summary = "Update an existing partner")
    public ResponseEntity<ApiResponse<PartnerResponseDto>> updatePartner(
            @Valid @RequestBody PartnerRequestDto request) {
        log.info("Updating partner: {}", request.getPartnerReferenceNumber());
        ResponseEntity<ApiResponse<PartnerResponseDto>> response = partnerService.updatePartner(request);
        auditService.log(null, "UPDATE_PARTNER", "PARTNER", null,
                "Partner updated: ref=" + request.getPartnerReferenceNumber());
        return response;
    }

    @PatchMapping("/v1/partners/{partnerReferenceNumber}/status")
    @Operation(summary = "Activate or deactivate a partner")
    public ResponseEntity<ApiResponse<PartnerResponseDto>> updatePartnerStatus(
            @PathVariable String partnerReferenceNumber,
            @Valid @RequestBody PartnerStatusUpdateRequestDto request,
            Authentication authentication) {
        String actor = SecurityUtils.getUsername(authentication);
        ResponseEntity<ApiResponse<PartnerResponseDto>> response =
                partnerService.updatePartnerStatus(partnerReferenceNumber, request.getPartnerStatus());
        auditService.log(actor,
                "INACTIVE".equalsIgnoreCase(request.getPartnerStatus()) ? "DEACTIVATE_PARTNER" : "ACTIVATE_PARTNER",
                "PARTNER", null,
                "Partner " + partnerReferenceNumber + " status set to " + request.getPartnerStatus());
        return response;
    }

}