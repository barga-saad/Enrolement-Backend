package org.bcp.dlmsenrolment.excptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlePartnerNotFound(PartnerNotFoundException ex) {
        log.error("PartnerNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "PARTNER_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(PartnerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handlePartnerExists(PartnerAlreadyExistsException ex) {
        log.error("PartnerAlreadyExistsException: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "PARTNER_ALREADY_EXISTS", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidRequestBodyException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidRequestBody(InvalidRequestBodyException ex) {
        log.error("InvalidRequestBodyException: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_BODY", ex.getMessage(), null);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConversionFailedException.class })
    public ResponseEntity<ApiResponse<Object>> handleDateParsingError(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format(
                "Invalid format for parameter '%s'. Received value: '%s'. Expected format is yyyy-MM-dd.",
                ex.getName(), ex.getValue()
        );
        log.error("MethodArgumentTypeMismatchException: {}", errorMessage);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_DATE_FORMAT", errorMessage, ex.getName());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedOp(UnsupportedOperationException ex) {
        log.error("UnsupportedOperationException: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "UNSUPPORTED_OPERATION", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Get the first validation error for a concise message
        String field = ex.getBindingResult().getFieldErrors().get(0).getField();
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.error("MethodArgumentNotValidException on field '{}': {}", field, message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, field);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        // It's good practice to avoid exposing internal exception details to the client.
        log.error("Unhandled Exception: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected internal server error occurred.", null);
    }

    /**
     * Builds a standardized error response using the ApiResponse class.
     *
     * @param status      The HTTP status.
     * @param errorCode   A custom application-specific error code.
     * @param message     A descriptive error message.
     * @param field       The field related to the error, if applicable.
     * @return A ResponseEntity containing the structured ApiResponse.
     */
    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
            HttpStatus status, String errorCode, String message, String field) {

        ApiResponse<Object> apiResponse = ApiResponse.error(errorCode, message, field);
        return new ResponseEntity<>(apiResponse, status);
    }
}
