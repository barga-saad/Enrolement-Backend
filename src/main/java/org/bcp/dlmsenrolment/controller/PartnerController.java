package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.dtos.respons.TransferResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.PartnerService;
import org.bcp.dlmsenrolment.services.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * PartnerController - REST API endpoints for partner management.
 * 
 * SECURITY MODEL:
 * - All endpoints require valid JWT token from Keycloak
 * - Permissions enforced by PermissionAuthorizationFilter:
 *   - GET /partners/{id} requires PARTNER_VIEW_OWN (owner-check) or PARTNER_VIEW_ALL
 *   - GET /transfers/{id} requires TRANSFER_VIEW_OWN (owner-check) or TRANSFER_VIEW_ALL
 * 
 * OWNER-CHECK ENFORCEMENT:
 * - PARTNER_VIEW_OWN: User can only view partners they created
 * - TRANSFER_VIEW_OWN: User can only view transfers they initiated
 * - Enforced automatically by PermissionAuthorizationFilter
 * 
 * ROLES:
 * - PROFIL_SAISIE: Can view own partner/transfer records
 * - PROFIL_VALIDATEUR: Can view all partner/transfer records
 * - PROFIL_CONSULTATION: Can view all partner/transfer records (read-only)
 * - PROFIL_ADMIN_CENTRAL: Full access to all records
 * 
 */
@RestController
@Slf4j
@Tag(name = "Partner Management", description = "Partner and transfer endpoints with RBAC")
public class PartnerController {
    private final PartnerService partnerService;
    private final TransferService transferService;

    public PartnerController(PartnerService partnerService, TransferService transferService) {
        this.partnerService = partnerService;
        this.transferService = transferService;
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
     * GET /gealltransfers/{partnerId} - Get all transfers for a partner
     * <p>
     * SECURITY:
     * - Requires TRANSFER_VIEW_OWN (with owner-check) OR TRANSFER_VIEW_ALL permission
     * - If user has TRANSFER_VIEW_OWN: Only sees their own transfers
     * - If user has TRANSFER_VIEW_ALL: Can see all transfers for this partner
     *
     * @param partnerId      - Partner identifier
     * @param authentication - Current authenticated user (from JWT token)
     * @return List of transfer records
     */
    @GetMapping("gealltransfers/{partnerId}")
    @Operation(summary = "Get all transfers for partner",
               description = "Requires TRANSFER_VIEW_OWN (own records) or TRANSFER_VIEW_ALL (all records)")
    public ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfer(
            @PathVariable String partnerId,
            Authentication authentication) {
        log.info("User {} requested transfers for partner: {}", SecurityUtils.getUsername(authentication), partnerId);
        return transferService.getAllTransfersByPartnerReferenceNumber(partnerId);
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
        return partnerService.createPartner(request);
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
        return partnerService.updatePartner(request);
    }

    /**
     * DELETE /v1/delete/partners - Delete a partner
     * <p>
     * SECURITY:
     * - Requires PARTNER_DELETE permission
     *
     * @param partnerReferenceNumber - Partner reference number
     * @return Deleted partner data
     */
    @DeleteMapping("/v1/delete/partners")
    @Operation(summary = "Delete a partner")
    public ResponseEntity<ApiResponse<PartnerResponseDto>> deletePartner(
            @RequestParam String partnerReferenceNumber) {
        log.info("Deleting partner: {}", partnerReferenceNumber);
        return partnerService.deletePartner(partnerReferenceNumber);
    }
}