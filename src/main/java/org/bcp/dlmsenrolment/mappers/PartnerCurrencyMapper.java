package org.bcp.dlmsenrolment.mappers;

import org.bcp.dlmsenrolment.dtos.request.PartnerCurrencyRequestDto;
import org.bcp.dlmsenrolment.entities.PartnerCurrency;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartnerCurrencyMapper {
    PartnerCurrency toEntity(PartnerCurrencyRequestDto partnerCurrencyRequestDto);
    PartnerCurrencyRequestDto toDto(PartnerCurrency partnerCurrency);
    List<PartnerCurrencyRequestDto> toDtoList(List<PartnerCurrency> entities);
}
