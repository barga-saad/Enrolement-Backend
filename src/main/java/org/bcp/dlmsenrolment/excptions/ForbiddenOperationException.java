package org.bcp.dlmsenrolment.excptions;

/**
 * Thrown when the current user is authenticated and has the endpoint permission, but a business
 * rule forbids this particular operation (e.g. an habilitation agent granting PROFIL_ADMIN_CENTRAL).
 * Mapped to HTTP 403 by {@link GlobalExceptionHandler}.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
