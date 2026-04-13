package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.AuditLog;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.repositories.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit trail endpoints (admin only)")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Get all audit logs (paginated)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved.", logs));
    }

    @GetMapping("/by-entity")
    @Operation(summary = "Get audit logs by entity type (paginated)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByEntityType(
            @RequestParam String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogRepository.findByEntityType(entityType, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved.", logs));
    }

    @GetMapping("/by-action")
    @Operation(summary = "Get audit logs by action")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByAction(@RequestParam String action) {
        List<AuditLog> logs = auditLogRepository.findByAction(action);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved.", logs));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get audit logs by date range")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<AuditLog> logs = auditLogRepository.findByTimestampRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved.", logs));
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get audit logs for a user (paginated)")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved.", logs));
    }
}
