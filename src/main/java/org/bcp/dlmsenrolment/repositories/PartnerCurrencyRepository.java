package org.bcp.dlmsenrolment.repositories;



import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.entities.PartnerCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PartnerCurrencyRepository extends JpaRepository<PartnerCurrency, Long> {
    PartnerCurrency findByPartner(Partner partner);

    List<PartnerCurrency> findAllByPartner(Partner partner);

    @Query("SELECT pc.currency FROM PartnerCurrency pc WHERE pc.partner = :partner")
    Set<String> findCurrencyCodesByPartner(@Param("partner") Partner partner);

    void deleteAllByPartnerAndCurrencyIn(Partner partner, Set<String> currencies);

}
