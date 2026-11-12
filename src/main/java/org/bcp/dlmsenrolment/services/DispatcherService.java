package org.bcp.dlmsenrolment.services;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface DispatcherService  {
    ResponseEntity<?> create(String tableName, HashMap<String, Object> body);
    ResponseEntity<?> read(String tableName, String partnerReferenceNumber);
    ResponseEntity<?> update(String tableName, HashMap<String, Object> body);
}
