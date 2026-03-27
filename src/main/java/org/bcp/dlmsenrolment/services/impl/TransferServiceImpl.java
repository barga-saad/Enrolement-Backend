package org.bcp.dlmsenrolment.services.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.bcp.dlmsenrolment.dtos.request.TransferRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.TransferResponseDto;
import org.bcp.dlmsenrolment.entities.Transfer;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.mappers.TransferMapper;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.repositories.TransferRepository;
import org.bcp.dlmsenrolment.services.SecurityTokenService;
import org.bcp.dlmsenrolment.services.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@Slf4j
@Transactional
public class TransferServiceImpl implements TransferService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final TransferMapper transferMapper;
    private final TransferRepository transferRepository;
    private final PartnerRepository partnerRepository;
    private final SecurityTokenService securityTokenService;

    public TransferServiceImpl(TransferMapper transferMapper, TransferRepository transferRepository, PartnerRepository partnerRepository, SecurityTokenService securityTokenService) {
        this.transferMapper = transferMapper;
        this.transferRepository = transferRepository;
        this.partnerRepository = partnerRepository;
        this.securityTokenService = securityTokenService;
    }




    @Override
    public ResponseEntity<ApiResponse<TransferRequestDto>> getTransfer(String transferId) {
        //this is the endpoint status.
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerReferenceNumber(String partnerReferenceNumber) {
        List<TransferResponseDto> response = transferRepository
                .findAllByPartnerReferenceNumber(partnerReferenceNumber)
                .stream()
                .map(transferMapper::toResponseDto)
                .toList();

        String successMessage = String.format("Found %d transfer(s) for partner '%s'.", response.size(), partnerReferenceNumber);

        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    }


    @Override
    public ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerIdToday(String partnerReferenceNumber, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1); // 23:59:59.999999999

        List<Transfer> transfers = transferRepository
                .findAllByPartnerReferenceNumberAndCreatedAtBetween(partnerReferenceNumber, startOfDay, endOfDay);


        // Convert entities to DTOs (assuming a mapper exists)
        List<TransferResponseDto> response = transfers.stream()
                .map(transferMapper::toResponseDto)
                .toList();
        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String successMessage = String.format(
                "Found %d transfer(s) for partner '%s' on %s.",
                response.size(),
                partnerReferenceNumber,
                formattedDate
        );

        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    }

    @Override
    public ResponseEntity<ApiResponse<List<TransferResponseDto>>> getAllTransfersByPartnerIdAndStartDateAndEndDate(
            String referenceNumber, LocalDate startDate, LocalDate endDate
    ) {
        // Convert to LocalDateTime for filtering
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1); // inclusive

        List<Transfer> transfers = transferRepository
                .findByPartnerReferenceNumberAndCreatedAtBetween(referenceNumber, startDateTime, endDateTime);

        // Convert entities to DTOs (assuming a mapper exists)
        List<TransferResponseDto> response = transfers.stream()
                .map(transferMapper::toResponseDto)
                .toList();
        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String successMessage = String.format(
                "Found %d transfer(s) for partner '%s' between %s and %s.",
                response.size(),
                referenceNumber,
                formattedStartDate,
                formattedEndDate
        );

        return ResponseEntity.ok(ApiResponse.success(successMessage, response));
    }



}
