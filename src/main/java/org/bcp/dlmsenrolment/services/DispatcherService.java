package org.bcp.dlmsenrolment.services;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;

public interface DispatcherService  {
    ResponseEntity<?> create(String tableName, HashMap<String, Object> body);
    ResponseEntity<?> delete(String tableName, String referenceNumber);
    ResponseEntity<?> read(String tableName, String partnerReferenceNumber, LocalDate startDate, LocalDate endDate);
    ResponseEntity<?> update(String tableName, HashMap<String, Object> body);
}
