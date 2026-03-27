package org.bcp.dlmsenrolment.controller;

import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.services.DispatcherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/v1")
public class DispatcherController {
    private final DispatcherService dispatcherService;

    public DispatcherController(DispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }
    @PostMapping("/create/{tableName}")
    public ResponseEntity<?> create(@PathVariable String tableName, @RequestBody HashMap<String, Object> body) {
        log.info("Received request to create table {}", tableName);
        log.info("Received body: {}", body);
        return dispatcherService.create(tableName, body);
    }


    @GetMapping("/read/{tableName}")
    public ResponseEntity<?> read(
            @PathVariable String tableName,
            @RequestParam(required = false) String partnerReferenceNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return dispatcherService.read(tableName, partnerReferenceNumber, startDate, endDate);
    }

    @PutMapping("/update/{tableName}")
    public ResponseEntity<?> update(@PathVariable String tableName, @RequestBody HashMap<String, Object> body) {
        return dispatcherService.update(tableName, body);
    }

    @DeleteMapping("/delete/{tableName}")
    public ResponseEntity<?> delete(@PathVariable String tableName,
                                    @RequestParam String partnerReferenceNumber) {
        return dispatcherService.delete(tableName, partnerReferenceNumber);
    }
}
