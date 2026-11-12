package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.services.DispatcherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * DispatcherController - Generic REST endpoint for dynamic CRUD operations.
 * 
 * Handles generic create/read/update operations for supported resources.
 * All endpoints are secured with RBAC authentication.
 * 
 * SECURITY:
 * - All requests require valid JWT token from Keycloak
 * - Permissions enforced by PermissionAuthorizationFilter
 * - User context available via Authentication parameter
 * 
 */
@Slf4j
@RestController
@RequestMapping("/v1")
@Tag(name = "Dispatcher", description = "Generic CRUD operations")
public class DispatcherController {
    private final DispatcherService dispatcherService;

    public DispatcherController(DispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    /**
     * POST /v1/create/{tableName} - Create new record
     * 
     * Requires: Appropriate CREATE permission (e.g., PARTNER_CREATE)
     * Audit: User who created the record is captured automatically
     */
    @PostMapping("/create/{tableName}")
    @Operation(summary = "Create new record")
    public ResponseEntity<?> create(
            @PathVariable String tableName,
            @RequestBody HashMap<String, Object> body,
            Authentication authentication) {
        log.info("User {} received request to create record in {}", authentication.getName(), tableName);
        log.info("Request body: {}", body);
        return dispatcherService.create(tableName, body);
    }

    /**
     * GET /v1/read/{tableName} - Read records
     * 
     * Requires: Appropriate READ permission (e.g., PARTNER_LIST_ALL or PARTNER_VIEW_OWN)
     * Filtering: If user has VIEW_OWN permission, only their records returned
     * 
    * @param tableName - Table to query (partners, partner currencies)
     * @param partnerReferenceNumber - Filter by partner reference (optional)
     */
    @GetMapping("/read/{tableName}")
    @Operation(summary = "Read records with optional filters")
    public ResponseEntity<?> read(
            @PathVariable String tableName,
            @RequestParam(required = false) String partnerReferenceNumber,
            Authentication authentication) {
        log.info("User {} requested read from {}", authentication.getName(), tableName);
        return dispatcherService.read(tableName, partnerReferenceNumber);
    }

    /**
     * PUT /v1/update/{tableName} - Update record
     * 
     * Requires: Appropriate UPDATE permission
     * Owner-Check: If requires_owner_check=TRUE, user must be the creator
     * Audit: User who updated is captured automatically
     * 
     * @param tableName - Table to update
     * @param body - Updated data
     */
    @PutMapping("/update/{tableName}")
    @Operation(summary = "Update existing record")
    public ResponseEntity<?> update(
            @PathVariable String tableName,
            @RequestBody HashMap<String, Object> body,
            Authentication authentication) {
        log.info("User {} received request to update record in {}", authentication.getName(), tableName);
        return dispatcherService.update(tableName, body);
    }

    @DeleteMapping("/delete/{tableName}")
    @Operation(summary = "Deprecated generic delete endpoint")
    public ResponseEntity<?> delete() {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "error", "ENDPOINT_DEPRECATED",
                "message", "Generic deletion is disabled. Use a resource-specific action."));
    }
}
