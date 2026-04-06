package org.bcp.dlmsenrolment.mappers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.bcp.dlmsenrolment.dtos.request.PartnerCurrencyRequestDto;
import org.bcp.dlmsenrolment.entities.PartnerCurrency;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-06T16:10:30+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PartnerCurrencyMapperImpl implements PartnerCurrencyMapper {

    @Override
    public PartnerCurrency toEntity(PartnerCurrencyRequestDto partnerCurrencyRequestDto) {
        if ( partnerCurrencyRequestDto == null ) {
            return null;
        }

        PartnerCurrency partnerCurrency = new PartnerCurrency();

        return partnerCurrency;
    }

    @Override
    public PartnerCurrencyRequestDto toDto(PartnerCurrency partnerCurrency) {
        if ( partnerCurrency == null ) {
            return null;
        }

        PartnerCurrencyRequestDto partnerCurrencyRequestDto = new PartnerCurrencyRequestDto();

        return partnerCurrencyRequestDto;
    }

    @Override
    public List<PartnerCurrencyRequestDto> toDtoList(List<PartnerCurrency> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PartnerCurrencyRequestDto> list = new ArrayList<PartnerCurrencyRequestDto>( entities.size() );
        for ( PartnerCurrency partnerCurrency : entities ) {
            list.add( toDto( partnerCurrency ) );
        }

        return list;
    }
}
