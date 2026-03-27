package org.bcp.dlmsenrolment.mappers;

import org.bcp.dlmsenrolment.dtos.request.TransferRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.TransferResponseDto;
import org.bcp.dlmsenrolment.entities.Transfer;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface TransferMapper {

    TransferRequestDto toRequestDto(Transfer transfer);

    TransferResponseDto toResponseDto(Transfer transfer);
}
