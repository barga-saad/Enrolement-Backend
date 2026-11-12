package org.bcp.dlmsenrolment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.AccountValidationRequestDto;
import org.bcp.dlmsenrolment.services.AccountValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
public class AccountValidationController {

    private final AccountValidationService accountValidationService;

    public AccountValidationController(AccountValidationService accountValidationService) {
        this.accountValidationService = accountValidationService;
    }

    @PostMapping("/v1/partners/validate-account")
    public ResponseEntity<?> validateAccount(@Valid @RequestBody AccountValidationRequestDto request) {
        try {
            JsonNode validationStatus = accountValidationService.validateAccount(request.accountNumber());
            return ResponseEntity.ok(validationStatus);
        } catch (RestClientException | IllegalStateException exception) {
            String networkCause = "";
            if (exception instanceof ResourceAccessException) {
                Throwable cause = exception;
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                networkCause = ", cause=" + cause.getClass().getSimpleName()
                        + ": " + Objects.toString(cause.getMessage(), "");
            }
            log.warn("Account validation service unavailable ({}{})",
                    exception.getClass().getSimpleName(), networkCause);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("code", "ACCOUNT_VALIDATION_UNAVAILABLE"));
        }
    }
}