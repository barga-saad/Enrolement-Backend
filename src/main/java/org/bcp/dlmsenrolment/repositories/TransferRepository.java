package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    //transfer api
    boolean existsByEndToEndIdAndPartnerReferenceNumberAndOperationType(String endToEndId, String partnerRef, String currency);
    Optional<Transfer> findByEndToEndIdAndPartnerReferenceNumberAndOperationType(String endToEndId, String partnerReferenceNumber, String operationType);
    Optional<Transfer> findByEndToEndId(String endToEndId);
    Optional<Transfer> findByPartnerReferenceNumberAndEndToEndId(String partnerReferenceNumber, String endToEndId);
    Optional<Transfer> findByOperationTypeAndPartnerReferenceNumberAndEndToEndId(String operationType, String partnerReferenceNumber, String endToEndId);
    //consultation db
    List<Transfer> findAllByPartnerReferenceNumber(String partnerReferenceNumber);
    List<Transfer> findAllByPartnerReferenceNumberAndCreatedAtBetween(String partnerReferenceNumber, LocalDateTime createdAt, LocalDateTime createdAt2);
    List<Transfer> findByPartnerReferenceNumberAndCreatedAtBetween(String referenceNumber, LocalDateTime start, LocalDateTime end);

}
