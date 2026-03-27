package org.bcp.dlmsenrolment.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerCurrencyRequestDto;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.mappers.DtoMapper;
import org.bcp.dlmsenrolment.services.DispatcherService;
import org.bcp.dlmsenrolment.services.PartnerCurrencyService;
import org.bcp.dlmsenrolment.services.PartnerService;
import org.bcp.dlmsenrolment.services.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
//import jakarta.validation.*;



@Service
@Slf4j
public class DispatcherServiceImpl implements DispatcherService {

    private final PartnerService partnerService;
    private final PartnerCurrencyService partnerCurrencyService;
    private final TransferService transferService;
//    private final Validator validator;
    private final ObjectMapper objectMapper;

    public DispatcherServiceImpl(PartnerService partnerService, PartnerCurrencyService partnerCurrencyService, TransferService transferService, ObjectMapper objectMapper) {
        this.partnerService = partnerService;
        this.partnerCurrencyService = partnerCurrencyService;
        this.transferService = transferService;
//        this.validator = validator;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<?> create(String tableName, HashMap<String, Object> body) {
        if ("partner".equalsIgnoreCase(tableName)) {
            return createPartner(body);
        } else if ("partnercurrency".equalsIgnoreCase(tableName)) {
            return createPartnerCurrency(body);
        } else {
            throw new UnsupportedOperationException("Create not supported for table: " + tableName);
        }
    }

    @Override
    public ResponseEntity<?> delete(String tableName, String referenceNumber) {
        if ("partner".equalsIgnoreCase(tableName)) {
            return partnerService.deletePartner(referenceNumber);
        } else if ("partnercurrency".equalsIgnoreCase(tableName)) {
            return partnerCurrencyService.deletePartnerCurrencies(referenceNumber);
        } else {
            throw new UnsupportedOperationException("Delete not supported for table: " + tableName);
        }
    }

    @Override
    public ResponseEntity<?> read(String tableName, String partnerReferenceNumber, LocalDate startDate, LocalDate endDate) {
        if ("partner".equalsIgnoreCase(tableName)) {
            if (partnerReferenceNumber != null) {
                return partnerService.getPartner(partnerReferenceNumber);
            }
            return partnerService.getPartners();

        } else if ("partnercurrency".equalsIgnoreCase(tableName)) {
            return partnerCurrencyService.getPartnerCurrencies(partnerReferenceNumber);
        } else if ("transfer".equalsIgnoreCase(tableName)) {
            if (startDate != null && endDate != null) {
                return transferService.getAllTransfersByPartnerIdAndStartDateAndEndDate(partnerReferenceNumber, startDate, endDate);
            }
            return transferService.getAllTransfersByPartnerReferenceNumber(partnerReferenceNumber);
        } else {
            throw new UnsupportedOperationException("Read not supported for table: " + tableName);
        }
    }

    @Override
    public ResponseEntity<?> update(String tableName, HashMap<String, Object> body) {
        if ("partner".equalsIgnoreCase(tableName)) {
            return updatePartner(body);
        } else if ("partnercurrency".equalsIgnoreCase(tableName)) {
            return updatePartnerCurrency(body);
        } else {
            throw new UnsupportedOperationException("Update not supported for table: " + tableName);
        }
    }


    private ResponseEntity<?> createPartner(HashMap<String, Object> body) {
    PartnerRequestDto partnerRequestDto = DtoMapper.mapToDto(body, PartnerRequestDto.class);
    return partnerService.createPartner(partnerRequestDto);
    }



    private ResponseEntity<?> createPartnerCurrency(HashMap<String, Object> body) {
        PartnerCurrencyRequestDto partnerDto = DtoMapper.mapToDto(body, PartnerCurrencyRequestDto.class);
        return partnerCurrencyService.createPartnerCurrency(partnerDto);
    }



    private ResponseEntity<?> updatePartner(HashMap<String, Object> body) {
        PartnerRequestDto partnerRequestDto = DtoMapper.mapToDto(body, PartnerRequestDto.class);
        return partnerService.updatePartner(partnerRequestDto);
    }



    private ResponseEntity<?> updatePartnerCurrency(HashMap<String, Object> body) {
        PartnerCurrencyRequestDto partnerDto = DtoMapper.mapToDto(body, PartnerCurrencyRequestDto.class);
        return partnerCurrencyService.updatePartnerCurrencies(partnerDto);
    }


}
