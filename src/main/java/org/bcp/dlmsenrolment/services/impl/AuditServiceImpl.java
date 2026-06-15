package org.bcp.dlmsenrolment.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.AuditLog;
import org.bcp.dlmsenrolment.repositories.AuditLogRepository;
import org.bcp.dlmsenrolment.services.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuditServiceImpl — Writes immutable audit log entries.
 *
 * Uses REQUIRES_NEW propagation so audit logs are persisted even
 * if the calling transaction rolls back (e.g. a failed login attempt).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, String action, String entityType, Long entityId, String description) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUsername(username);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setDescription(description);
            auditLogRepository.save(entry);
            log.debug("Audit: [{} | {} | {} | {}] {}", action, entityType, entityId, username, description);
        } catch (Exception ex) {
            // Audit must never crash the main flow
            log.error("Failed to write audit log [{} {} {}]: {}", action, entityType, entityId, ex.getMessage());
        }
    }
}
