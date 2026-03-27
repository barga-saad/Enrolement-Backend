package org.bcp.dlmsenrolment.mappers;

import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.entities.Partner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartnerMapper {
    Partner toEntity(PartnerRequestDto dto);

    PartnerRequestDto toRequestDto(Partner entity);

    PartnerResponseDto toResponseDto(Partner entity);

}
