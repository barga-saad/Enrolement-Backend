package org.bcp.dlmsenrolment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates sequential, unique partner reference numbers.
 *
 * Format  : 6-digit zero-padded integer string (e.g. "043000")
 * Counter : starts at 042999 → first generated value is "043000"
 *
 * Concurrency safety:
 *   - SERIALIZABLE isolation prevents phantom reads in concurrent transactions.
 *   - synchronized ensures single-JVM thread safety (single-node deployment).
 *   - For multi-node deployments, replace with a PostgreSQL SEQUENCE via native query.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerReferenceNumberService {

    /** Counter floor — existing prod data stops at 042999. */
    private static final int STARTING_VALUE = 42999;

    private final PartnerRepository partnerRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized String generateNextReferenceNumber() {
        Integer maxRef = partnerRepository.findMaxNumericReferenceNumber();
        int next = (maxRef != null ? maxRef : STARTING_VALUE) + 1;
        String refNumber = String.format("%06d", next);
        log.info("Generated reference number: {}", refNumber);
        return refNumber;
    }
}
