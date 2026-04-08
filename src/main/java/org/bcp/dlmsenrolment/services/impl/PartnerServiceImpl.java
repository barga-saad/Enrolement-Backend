package org.bcp.dlmsenrolment.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.excptions.PartnerAlreadyExistsException;
import org.bcp.dlmsenrolment.excptions.PartnerNotFoundException;
import org.bcp.dlmsenrolment.mappers.PartnerMapper;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.services.PartnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;

    public PartnerServiceImpl(PartnerRepository partnerRepository, PartnerMapper partnerMapper) {
        this.partnerRepository = partnerRepository;
        this.partnerMapper = partnerMapper;
    }
    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> getPartner(String referenceNumber) {
        Partner partner = partnerRepository.findByPartnerReferenceNumber(referenceNumber)
                .orElseThrow(() -> new PartnerNotFoundException(
                        "No partner found with reference number: " + referenceNumber
                ));
        return ResponseEntity.ok(ApiResponse.success("Partner retrieved successfully.", partnerMapper.toResponseDto(partner)));
    }

    @Override
    public ResponseEntity<ApiResponse<List<PartnerRequestDto>>> getPartners() {
        List<Partner> partners = partnerRepository.findAll();

        List<PartnerRequestDto> partnerRequestDtos = partners.stream()
                .map(partnerMapper::toRequestDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Partners retrieved successfully.", partnerRequestDtos));
    }


    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> createPartner(PartnerRequestDto partnerRequestDto) {
        Optional<Partner> existing = partnerRepository.findByPartnerReferenceNumber(partnerRequestDto.getPartnerReferenceNumber());
        if (existing.isPresent()){
            throw new PartnerAlreadyExistsException(
                    "Partner already exists with reference number: " + partnerRequestDto.getPartnerReferenceNumber()
            );
        }
        Partner savedPartner = partnerRepository.save(partnerMapper.toEntity(partnerRequestDto));
        return ResponseEntity.ok(ApiResponse.success("Partner created successfully.", partnerMapper.toResponseDto(savedPartner)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> updatePartner(PartnerRequestDto partnerRequestDto) {
        Partner partner = partnerRepository.findByPartnerReferenceNumber(partnerRequestDto.getPartnerReferenceNumber())
                .orElseThrow(() -> new PartnerNotFoundException(
                        "Cannot update. No partner found with reference number: " + partnerRequestDto.getPartnerReferenceNumber()
                ));
        Partner updated = partnerRepository.save(updatePartnerFromDto(partner, partnerRequestDto));
        return ResponseEntity.ok(ApiResponse.success("Partner updated successfully.", partnerMapper.toResponseDto(updated)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> deletePartner(String referenceNumber) {
        Partner partner = partnerRepository.findByPartnerReferenceNumber(referenceNumber)
                .orElseThrow(() -> new PartnerNotFoundException(
                        "Cannot delete. No partner found with reference number: " + referenceNumber
                ));
        PartnerResponseDto response = partnerMapper.toResponseDto(partner);
        partnerRepository.delete(partner);
        return ResponseEntity.ok(ApiResponse.success("Partner deleted successfully.", response));
    }


    public Partner updatePartnerFromDto(Partner partner, PartnerRequestDto dto) {
        partner.setName(dto.getName());
        partner.setPartnerReferenceNumber(dto.getPartnerReferenceNumber());
        partner.setPartnerType(dto.getPartnerType());
        partner.setPartnerStatus(dto.getPartnerStatus());
        partner.setHeadquarterCountry(dto.getHeadquarterCountry());
        partner.setParentCompanyCountry(dto.getParentCompanyCountry());
        partner.setClassification(dto.getClassification());
        partner.setAccountNumber(dto.getAccountNumber());
//        partner.setCurrencies(dto.getCurrencies().stream()
//                .map(this::convertToPartnerCurrency) // You need to implement this method to convert DTO currency to entity
//                .collect(Collectors.toSet()));
        partner.setCanal(dto.getCanal());
        partner.setAllowedInternalInstantTransfer(dto.isAllowedInternalInstantTransfer());
        partner.setLimitInternalInstantTransfer(dto.getLimitInternalInstantTransfer());
        partner.setAllowedExternalInstantTransfer(dto.isAllowedExternalInstantTransfer());
        partner.setLimitExternalInstantTransfer(dto.getLimitExternalInstantTransfer());
        partner.setAllowedStandardTransfer(dto.isAllowedStandardTransfer());
        partner.setLimitStandardTransfer(dto.getLimitStandardTransfer());
        partner.setAllowedCashPickupTransfer(dto.isAllowedCashPickupTransfer());
        partner.setLimitCashPickupTransfer(dto.getLimitCashPickupTransfer());
        partner.setAllowedMassCashPickupTransfer(dto.isAllowedMassCashPickupTransfer());
        partner.setLimitMassCashPickupTransfer(dto.getLimitMassCashPickupTransfer());
        partner.setNatureMassCashPickupTransfer(dto.getNatureMassCashPickupTransfer());
        partner.setValidDaysMassCashPickupTransfer(dto.getValidDaysMassCashPickupTransfer());
        partner.setAllowedWalletTransfer(dto.isAllowedWalletTransfer());
        partner.setLimitWalletTransfer(dto.getLimitWalletTransfer());
        partner.setMainCurrency(dto.getMainCurrency());
        partner.setExchangeRateNature(dto.getExchangeRateNature());
        partner.setSubscriptionFee(dto.getSubscriptionFee());
        partner.setSubscriptionPeriodicity(dto.getSubscriptionPeriodicity());
        partner.setSubscriptionBillingAccount(dto.getSubscriptionBillingAccount());
        partner.setCommissionCurrency(dto.getCommissionCurrency());
        partner.setExchangeGainPercentage(dto.getExchangeGainPercentage());
        partner.setPaymentNature(dto.getPaymentNature());
        partner.setFeeMode(dto.getFeeMode());
        partner.setMonthlyBilling(dto.getMonthlyBilling());
        partner.setImmediateDebit(dto.getImmediateDebit());
        partner.setReimbursement(dto.getReimbursement());
        partner.setCommissionTypeInstantInternal(dto.getCommissionTypeInstantInternal());
        partner.setCommissionAmountInstantInternal(dto.getCommissionAmountInstantInternal());
        partner.setCommissionRateInstantInternal(dto.getCommissionRateInstantInternal());
        partner.setCommissionMinInstantInternal(dto.getCommissionMinInstantInternal());
        partner.setCommissionMaxInstantInternal(dto.getCommissionMaxInstantInternal());
        partner.setCommissionTypeInstantExternal(dto.getCommissionTypeInstantExternal());
        partner.setCommissionAmountInstantExternal(dto.getCommissionAmountInstantExternal());
        partner.setCommissionRateInstantExternal(dto.getCommissionRateInstantExternal());
        partner.setCommissionMinInstantExternal(dto.getCommissionMinInstantExternal());
        partner.setCommissionMaxInstantExternal(dto.getCommissionMaxInstantExternal());
        partner.setCommissionTypeStandardExternal(dto.getCommissionTypeStandardExternal());
        partner.setCommissionAmountStandardExternal(dto.getCommissionAmountStandardExternal());
        partner.setCommissionRateStandardExternal(dto.getCommissionRateStandardExternal());
        partner.setCommissionMinStandardExternal(dto.getCommissionMinStandardExternal());
        partner.setCommissionMaxStandardExternal(dto.getCommissionMaxStandardExternal());
        partner.setCommissionTypeMassCashPickup(dto.getCommissionTypeMassCashPickup());
        partner.setCommissionAmountMassCashPickup(dto.getCommissionAmountMassCashPickup());
        partner.setCommissionRateMassCashPickup(dto.getCommissionRateMassCashPickup());
        partner.setCommissionMinMassCashPickup(dto.getCommissionMinMassCashPickup());
        partner.setCommissionMaxMassCashPickup(dto.getCommissionMaxMassCashPickup());
        partner.setCommissionTypeCashPickup(dto.getCommissionTypeCashPickup());
        partner.setCommissionAmountCashPickup(dto.getCommissionAmountCashPickup());
        partner.setCommissionRateCashPickup(dto.getCommissionRateCashPickup());
        partner.setCommissionMinCashPickup(dto.getCommissionMinCashPickup());
        partner.setCommissionMaxCashPickup(dto.getCommissionMaxCashPickup());
        partner.setCommissionTypeWallet(dto.getCommissionTypeWallet());
        partner.setCommissionAmountWallet(dto.getCommissionAmountWallet());
        partner.setCommissionRateWallet(dto.getCommissionRateWallet());
        partner.setCommissionMinWallet(dto.getCommissionMinWallet());
        partner.setCommissionMaxWallet(dto.getCommissionMaxWallet());
        return partner;
    }

}
