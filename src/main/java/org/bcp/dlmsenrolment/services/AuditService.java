package org.bcp.dlmsenrolment.services;

/**
 * AuditService — Central service for writing audit log entries.
 *
 * Inject this wherever you need to trace an action:
 *   auditService.log(username, "CREATE_USER", "USER", userId, "User john created");
 *
 * Actions used across the platform:
 *   Auth      : LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, PASSWORD_CHANGE, TOKEN_REFRESH
 *   Partner   : CREATE_PARTNER, UPDATE_PARTNER, DELETE_PARTNER
 *   Validation: SUBMIT_VALIDATION, VALIDATE, REJECT
 *   User      : CREATE_USER, UPDATE_USER, DELETE_USER, ASSIGN_ROLE, REVOKE_ROLE
 *   Role      : CREATE_ROLE, UPDATE_ROLE, DELETE_ROLE, ASSIGN_PERMISSION, REMOVE_PERMISSION
 */
public interface AuditService {

    /**
     * Log a platform action.
     *
     * @param username    Username who performed the action (null for anonymous)
     * @param action      Verb in UPPER_SNAKE_CASE (e.g. "CREATE_USER")
     * @param entityType  Table/domain (e.g. "USER", "PARTNER", "ROLE")
     * @param entityId    Primary key of the affected record (null if N/A)
     * @param description Human-readable description of what happened
     */
    void log(String username, String action, String entityType, Long entityId, String description);
}
