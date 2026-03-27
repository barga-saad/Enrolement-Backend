package org.bcp.dlmsenrolment.repositories;

import org.bcp.dlmsenrolment.entities.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByPartnerReferenceNumber(String partnerReferenceNumber);

    @Query("SELECT COUNT(pc) > 0 FROM PartnerCurrency pc WHERE pc.partner.partnerReferenceNumber = :ref AND LOWER(pc.currency) = LOWER(:currency)")
    boolean existsCurrencyForPartnerRef(@Param("ref") String partnerReferenceNumber, @Param("currency") String currency);


}
