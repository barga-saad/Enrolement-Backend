package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AdminStatsController - aggregated counters for the admin dashboard.
 * Returns counts only, never partner data (guarded by ADMIN_PARTNER_STATS).
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Statistics", description = "Aggregated counters for the admin dashboard")
public class AdminStatsController {

    private final PartnerRepository partnerRepository;

    @GetMapping("/partners")
    @Operation(summary = "Partner counts by workflow status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPartnerStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", partnerRepository.count());
        stats.put("validated", partnerRepository.countByWorkflowStatus("VALIDATED"));
        stats.put("pending", partnerRepository.countByWorkflowStatus("PENDING"));
        stats.put("draft", partnerRepository.countByWorkflowStatus("DRAFT"));
        stats.put("rejected", partnerRepository.countByWorkflowStatus("REJECTED"));
        return ResponseEntity.ok(ApiResponse.success("Partner statistics retrieved.", stats));
    }
}
