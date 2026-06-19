package org.bcp.dlmsenrolment.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.entities.PartnerValidation;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.excptions.PartnerNotFoundException;
import org.bcp.dlmsenrolment.mappers.PartnerMapper;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.repositories.PartnerValidationRepository;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.PartnerReferenceNumberService;
import org.bcp.dlmsenrolment.services.PartnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {
    private final PartnerRepository partnerRepository;
    private final PartnerMapper partnerMapper;
    private final PartnerValidationRepository partnerValidationRepository;
    private final PartnerReferenceNumberService referenceNumberService;

    public PartnerServiceImpl(PartnerRepository partnerRepository,
                              PartnerMapper partnerMapper,
                              PartnerValidationRepository partnerValidationRepository,
                              PartnerReferenceNumberService referenceNumberService) {
        this.partnerRepository = partnerRepository;
        this.partnerMapper = partnerMapper;
        this.partnerValidationRepository = partnerValidationRepository;
        this.referenceNumberService = referenceNumberService;
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
    public ResponseEntity<ApiResponse<List<PartnerResponseDto>>> getPartners() {
        List<Partner> partners = partnerRepository.findAll();
        List<PartnerResponseDto> dtos = partners.stream()
                .map(partnerMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Partners retrieved successfully.", dtos));
    }


    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> createPartner(PartnerRequestDto partnerRequestDto) {
        // Auto-generate unique sequential reference number (042999 + 1 = 043000, ...)
        String refNumber = referenceNumberService.generateNextReferenceNumber();

        Partner partner = partnerMapper.toEntity(partnerRequestDto);
        partner.setPartnerReferenceNumber(refNumber);

        String username = getCurrentUsername();
        partner.setCreatedBy(username);
        partner.setWorkflowStatus("DRAFT");
        if (partner.getEnrollmentType() == null) {
            partner.setEnrollmentType("PARTNER");
        }

        Partner savedPartner = partnerRepository.save(partner);

        // Auto-create a DRAFT validation record
        PartnerValidation validation = PartnerValidation.builder()
                .partner(savedPartner)
                .workflowStatus("DRAFT")
                .actionType("CREATE")
                .createdBy(username)
                .build();
        partnerValidationRepository.save(validation);
        log.info("Created {} '{}' ref={} by user {}",
                savedPartner.getEnrollmentType(), savedPartner.getName(), refNumber, username);

        return ResponseEntity.ok(ApiResponse.success(
                savedPartner.getEnrollmentType() + " created successfully.",
                partnerMapper.toResponseDto(savedPartner)));
    }

    @Override
    public ResponseEntity<ApiResponse<PartnerResponseDto>> updatePartner(PartnerRequestDto partnerRequestDto) {
        Partner partner = partnerRepository.findByPartnerReferenceNumber(partnerRequestDto.getPartnerReferenceNumber())
                .orElseThrow(() -> new PartnerNotFoundException(
                        "Cannot update. No partner found with reference number: " + partnerRequestDto.getPartnerReferenceNumber()
                ));
        Partner updated = partnerRepository.save(updatePartnerFromDto(partner, partnerRequestDto));

        // Auto-create a PENDING validation for the update (requires re-validation)
        String username = getCurrentUsername();
        PartnerValidation validation = PartnerValidation.builder()
                .partner(updated)
                .workflowStatus("PENDING")
                .actionType("UPDATE")
                .createdBy(username)
                .build();
        partnerValidationRepository.save(validation);
        log.info("Auto-created PENDING validation for updated partner {} by user {}", updated.getName(), username);

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
        partner.setAllowedSrbmRtgsTransfer(dto.isAllowedSrbmRtgsTransfer());
        partner.setLimitSrbmRtgsTransfer(dto.getLimitSrbmRtgsTransfer());
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

        // Enrollment type (do not override with null)
        if (dto.getEnrollmentType() != null) partner.setEnrollmentType(dto.getEnrollmentType());

        // Forex - Reception
        partner.setAutoriserReception(dto.getAutoriserReception());
        partner.setDevisesReception(dto.getDevisesReception());
        partner.setPlafondReception(dto.getPlafondReception());

        // Forex - Emission
        partner.setAutoriserEmission(dto.getAutoriserEmission());
        partner.setTypeTransactionEmission(dto.getTypeTransactionEmission());
        partner.setAutorisationOC(dto.getAutorisationOC());
        partner.setRefAutorisationOC(dto.getRefAutorisationOC());
        partner.setDevisesEmission(dto.getDevisesEmission());
        partner.setNatureCoursChangeEmission(dto.getNatureCoursChangeEmission());
        partner.setPlafondMontantDevise(dto.getPlafondMontantDevise());

        // Conformité Bloc A
        partner.setConformiteDOTypePiece(dto.getConformiteDOTypePiece());
        partner.setConformiteDONumPiece(dto.getConformiteDONumPiece());
        partner.setConformiteDODateNaissance(dto.getConformiteDODateNaissance());
        partner.setConformiteDOPaysNaissance(dto.getConformiteDOPaysNaissance());
        partner.setConformiteDOAdresse(dto.getConformiteDOAdresse());
        partner.setConformiteDOVilleResidence(dto.getConformiteDOVilleResidence());

        // Conformité Bloc B
        partner.setConformiteBenefTypePiece(dto.getConformiteBenefTypePiece());
        partner.setConformiteBenefNumPiece(dto.getConformiteBenefNumPiece());
        partner.setConformiteBenefAdresse(dto.getConformiteBenefAdresse());
        partner.setConformiteBenefVilleResidence(dto.getConformiteBenefVilleResidence());
        partner.setConformiteBenefPaysResidence(dto.getConformiteBenefPaysResidence());

        // Fichier Synthèse
        partner.setFichierSynthese(dto.getFichierSynthese());

        // Facturation
        partner.setSetupFee(dto.getSetupFee());
        partner.setSetupFeeBilled(dto.getSetupFeeBilled());
        partner.setBillingMode(dto.getBillingMode());
        partner.setDerogation(dto.getDerogation());
        partner.setDerogationDetails(dto.getDerogationDetails());

        return partner;
    }

    private String getCurrentUsername() {
        return SecurityUtils.getCurrentUsername();
    }

}
