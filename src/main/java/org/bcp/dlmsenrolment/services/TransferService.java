package org.bcp.dlmsenrolment.services;
import org.bcp.dlmsenrolment.dtos.request.TransferRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.TransferResponseDto;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransferService {
    //consultation db services
    ResponseEntity<ApiResponse<TransferRequestDto> > getTransfer(String transferId);
    ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerReferenceNumber(String partnerReferenceNumber);
    ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerIdToday(String partnerId, LocalDate date);
    ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerIdAndStartDateAndEndDate(String id, LocalDate startDate, LocalDate endDate);

}
