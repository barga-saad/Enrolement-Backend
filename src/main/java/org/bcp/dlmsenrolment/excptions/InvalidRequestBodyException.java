package org.bcp.dlmsenrolment.excptions;

public class InvalidRequestBodyException extends RuntimeException {
    public InvalidRequestBodyException(String message) {
        super(message);
    }
}
