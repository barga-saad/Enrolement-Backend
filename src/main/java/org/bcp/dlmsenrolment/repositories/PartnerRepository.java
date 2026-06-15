package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByPartnerReferenceNumber(String partnerReferenceNumber);

    @Query("SELECT COUNT(pc) > 0 FROM PartnerCurrency pc WHERE pc.partner.partnerReferenceNumber = :ref AND LOWER(pc.currency) = LOWER(:currency)")
    boolean existsCurrencyForPartnerRef(@Param("ref") String partnerReferenceNumber, @Param("currency") String currency);

    /**
     * Returns the maximum numeric reference number currently stored.
     * Filters only pure 6-digit numeric references to avoid cast errors.
     * Returns 42999 (COALESCE fallback) when the table is empty.
     */
    @Query(value = "SELECT COALESCE(MAX(CAST(partner_reference_number AS INTEGER)), 42999) " +
                   "FROM partners " +
                   "WHERE partner_reference_number ~ '^[0-9]{6}$'", nativeQuery = true)
    Integer findMaxNumericReferenceNumber();

    List<Partner> findByEnrollmentType(String enrollmentType);

    List<Partner> findByWorkflowStatus(String workflowStatus);
}
