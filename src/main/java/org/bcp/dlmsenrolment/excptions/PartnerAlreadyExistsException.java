package org.bcp.dlmsenrolment.excptions;

public class PartnerAlreadyExistsException extends RuntimeException {
    public PartnerAlreadyExistsException(String message) {
        super(message);
    }
}