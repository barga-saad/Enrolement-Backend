package org.bcp.dlmsenrolment.mappers;

import javax.annotation.processing.Generated;
import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerResponseDto;
import org.bcp.dlmsenrolment.entities.Partner;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-06T16:10:30+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PartnerMapperImpl implements PartnerMapper {

    @Override
    public Partner toEntity(PartnerRequestDto dto) {
        if ( dto == null ) {
            return null;
        }

        Partner partner = new Partner();

        partner.setAccountNumber( dto.getAccountNumber() );
        partner.setAllowedCashPickupTransfer( dto.isAllowedCashPickupTransfer() );
        partner.setAllowedExternalInstantTransfer( dto.isAllowedExternalInstantTransfer() );
        partner.setAllowedInternalInstantTransfer( dto.isAllowedInternalInstantTransfer() );
        partner.setAllowedMassCashPickupTransfer( dto.isAllowedMassCashPickupTransfer() );
        partner.setAllowedStandardTransfer( dto.isAllowedStandardTransfer() );
        partner.setAllowedWalletTransfer( dto.isAllowedWalletTransfer() );
        partner.setCanal( dto.getCanal() );
        partner.setClassification( dto.getClassification() );
        partner.setCommissionAmountCashPickup( dto.getCommissionAmountCashPickup() );
        partner.setCommissionAmountInstantExternal( dto.getCommissionAmountInstantExternal() );
        partner.setCommissionAmountInstantInternal( dto.getCommissionAmountInstantInternal() );
        partner.setCommissionAmountMassCashPickup( dto.getCommissionAmountMassCashPickup() );
        partner.setCommissionAmountStandardExternal( dto.getCommissionAmountStandardExternal() );
        partner.setCommissionAmountWallet( dto.getCommissionAmountWallet() );
        partner.setCommissionCurrency( dto.getCommissionCurrency() );
        partner.setCommissionMaxCashPickup( dto.getCommissionMaxCashPickup() );
        partner.setCommissionMaxInstantExternal( dto.getCommissionMaxInstantExternal() );
        partner.setCommissionMaxInstantInternal( dto.getCommissionMaxInstantInternal() );
        partner.setCommissionMaxMassCashPickup( dto.getCommissionMaxMassCashPickup() );
        partner.setCommissionMaxStandardExternal( dto.getCommissionMaxStandardExternal() );
        partner.setCommissionMaxWallet( dto.getCommissionMaxWallet() );
        partner.setCommissionMinCashPickup( dto.getCommissionMinCashPickup() );
        partner.setCommissionMinInstantExternal( dto.getCommissionMinInstantExternal() );
        partner.setCommissionMinInstantInternal( dto.getCommissionMinInstantInternal() );
        partner.setCommissionMinMassCashPickup( dto.getCommissionMinMassCashPickup() );
        partner.setCommissionMinStandardExternal( dto.getCommissionMinStandardExternal() );
        partner.setCommissionMinWallet( dto.getCommissionMinWallet() );
        partner.setCommissionRateCashPickup( dto.getCommissionRateCashPickup() );
        partner.setCommissionRateInstantExternal( dto.getCommissionRateInstantExternal() );
        partner.setCommissionRateInstantInternal( dto.getCommissionRateInstantInternal() );
        partner.setCommissionRateMassCashPickup( dto.getCommissionRateMassCashPickup() );
        partner.setCommissionRateStandardExternal( dto.getCommissionRateStandardExternal() );
        partner.setCommissionRateWallet( dto.getCommissionRateWallet() );
        partner.setCommissionTypeCashPickup( dto.getCommissionTypeCashPickup() );
        partner.setCommissionTypeInstantExternal( dto.getCommissionTypeInstantExternal() );
        partner.setCommissionTypeInstantInternal( dto.getCommissionTypeInstantInternal() );
        partner.setCommissionTypeMassCashPickup( dto.getCommissionTypeMassCashPickup() );
        partner.setCommissionTypeStandardExternal( dto.getCommissionTypeStandardExternal() );
        partner.setCommissionTypeWallet( dto.getCommissionTypeWallet() );
        partner.setExchangeGainPercentage( dto.getExchangeGainPercentage() );
        partner.setExchangeRateNature( dto.getExchangeRateNature() );
        partner.setFeeMode( dto.getFeeMode() );
        partner.setHeadquarterCountry( dto.getHeadquarterCountry() );
        partner.setImmediateDebit( dto.getImmediateDebit() );
        partner.setLimitCashPickupTransfer( dto.getLimitCashPickupTransfer() );
        partner.setLimitExternalInstantTransfer( dto.getLimitExternalInstantTransfer() );
        partner.setLimitInternalInstantTransfer( dto.getLimitInternalInstantTransfer() );
        partner.setLimitMassCashPickupTransfer( dto.getLimitMassCashPickupTransfer() );
        partner.setLimitStandardTransfer( dto.getLimitStandardTransfer() );
        partner.setLimitWalletTransfer( dto.getLimitWalletTransfer() );
        partner.setMainCurrency( dto.getMainCurrency() );
        partner.setMonthlyBilling( dto.getMonthlyBilling() );
        partner.setName( dto.getName() );
        partner.setNatureMassCashPickupTransfer( dto.getNatureMassCashPickupTransfer() );
        partner.setParentCompanyCountry( dto.getParentCompanyCountry() );
        partner.setPartnerReferenceNumber( dto.getPartnerReferenceNumber() );
        partner.setPartnerStatus( dto.isPartnerStatus() );
        partner.setPartnerType( dto.getPartnerType() );
        partner.setPaymentNature( dto.getPaymentNature() );
        partner.setReimbursement( dto.getReimbursement() );
        partner.setSubscriptionBillingAccount( dto.getSubscriptionBillingAccount() );
        partner.setSubscriptionFee( dto.getSubscriptionFee() );
        partner.setSubscriptionPeriodicity( dto.getSubscriptionPeriodicity() );
        partner.setValidDaysMassCashPickupTransfer( dto.getValidDaysMassCashPickupTransfer() );

        return partner;
    }

    @Override
    public PartnerRequestDto toRequestDto(Partner entity) {
        if ( entity == null ) {
            return null;
        }

        PartnerRequestDto partnerRequestDto = new PartnerRequestDto();

        partnerRequestDto.setAccountNumber( entity.getAccountNumber() );
        partnerRequestDto.setAllowedCashPickupTransfer( entity.isAllowedCashPickupTransfer() );
        partnerRequestDto.setAllowedExternalInstantTransfer( entity.isAllowedExternalInstantTransfer() );
        partnerRequestDto.setAllowedInternalInstantTransfer( entity.isAllowedInternalInstantTransfer() );
        partnerRequestDto.setAllowedMassCashPickupTransfer( entity.isAllowedMassCashPickupTransfer() );
        partnerRequestDto.setAllowedStandardTransfer( entity.isAllowedStandardTransfer() );
        partnerRequestDto.setAllowedWalletTransfer( entity.isAllowedWalletTransfer() );
        partnerRequestDto.setCanal( entity.getCanal() );
        partnerRequestDto.setClassification( entity.getClassification() );
        partnerRequestDto.setCommissionAmountCashPickup( entity.getCommissionAmountCashPickup() );
        partnerRequestDto.setCommissionAmountInstantExternal( entity.getCommissionAmountInstantExternal() );
        partnerRequestDto.setCommissionAmountInstantInternal( entity.getCommissionAmountInstantInternal() );
        partnerRequestDto.setCommissionAmountMassCashPickup( entity.getCommissionAmountMassCashPickup() );
        partnerRequestDto.setCommissionAmountStandardExternal( entity.getCommissionAmountStandardExternal() );
        partnerRequestDto.setCommissionAmountWallet( entity.getCommissionAmountWallet() );
        partnerRequestDto.setCommissionCurrency( entity.getCommissionCurrency() );
        partnerRequestDto.setCommissionMaxCashPickup( entity.getCommissionMaxCashPickup() );
        partnerRequestDto.setCommissionMaxInstantExternal( entity.getCommissionMaxInstantExternal() );
        partnerRequestDto.setCommissionMaxInstantInternal( entity.getCommissionMaxInstantInternal() );
        partnerRequestDto.setCommissionMaxMassCashPickup( entity.getCommissionMaxMassCashPickup() );
        partnerRequestDto.setCommissionMaxStandardExternal( entity.getCommissionMaxStandardExternal() );
        partnerRequestDto.setCommissionMaxWallet( entity.getCommissionMaxWallet() );
        partnerRequestDto.setCommissionMinCashPickup( entity.getCommissionMinCashPickup() );
        partnerRequestDto.setCommissionMinInstantExternal( entity.getCommissionMinInstantExternal() );
        partnerRequestDto.setCommissionMinInstantInternal( entity.getCommissionMinInstantInternal() );
        partnerRequestDto.setCommissionMinMassCashPickup( entity.getCommissionMinMassCashPickup() );
        partnerRequestDto.setCommissionMinStandardExternal( entity.getCommissionMinStandardExternal() );
        partnerRequestDto.setCommissionMinWallet( entity.getCommissionMinWallet() );
        partnerRequestDto.setCommissionRateCashPickup( entity.getCommissionRateCashPickup() );
        partnerRequestDto.setCommissionRateInstantExternal( entity.getCommissionRateInstantExternal() );
        partnerRequestDto.setCommissionRateInstantInternal( entity.getCommissionRateInstantInternal() );
        partnerRequestDto.setCommissionRateMassCashPickup( entity.getCommissionRateMassCashPickup() );
        partnerRequestDto.setCommissionRateStandardExternal( entity.getCommissionRateStandardExternal() );
        partnerRequestDto.setCommissionRateWallet( entity.getCommissionRateWallet() );
        partnerRequestDto.setCommissionTypeCashPickup( entity.getCommissionTypeCashPickup() );
        partnerRequestDto.setCommissionTypeInstantExternal( entity.getCommissionTypeInstantExternal() );
        partnerRequestDto.setCommissionTypeInstantInternal( entity.getCommissionTypeInstantInternal() );
        partnerRequestDto.setCommissionTypeMassCashPickup( entity.getCommissionTypeMassCashPickup() );
        partnerRequestDto.setCommissionTypeStandardExternal( entity.getCommissionTypeStandardExternal() );
        partnerRequestDto.setCommissionTypeWallet( entity.getCommissionTypeWallet() );
        partnerRequestDto.setExchangeGainPercentage( entity.getExchangeGainPercentage() );
        partnerRequestDto.setExchangeRateNature( entity.getExchangeRateNature() );
        partnerRequestDto.setFeeMode( entity.getFeeMode() );
        partnerRequestDto.setHeadquarterCountry( entity.getHeadquarterCountry() );
        partnerRequestDto.setImmediateDebit( entity.getImmediateDebit() );
        partnerRequestDto.setLimitCashPickupTransfer( entity.getLimitCashPickupTransfer() );
        partnerRequestDto.setLimitExternalInstantTransfer( entity.getLimitExternalInstantTransfer() );
        partnerRequestDto.setLimitInternalInstantTransfer( entity.getLimitInternalInstantTransfer() );
        partnerRequestDto.setLimitMassCashPickupTransfer( entity.getLimitMassCashPickupTransfer() );
        partnerRequestDto.setLimitStandardTransfer( entity.getLimitStandardTransfer() );
        partnerRequestDto.setLimitWalletTransfer( entity.getLimitWalletTransfer() );
        partnerRequestDto.setMainCurrency( entity.getMainCurrency() );
        partnerRequestDto.setMonthlyBilling( entity.getMonthlyBilling() );
        partnerRequestDto.setName( entity.getName() );
        partnerRequestDto.setNatureMassCashPickupTransfer( entity.getNatureMassCashPickupTransfer() );
        partnerRequestDto.setParentCompanyCountry( entity.getParentCompanyCountry() );
        partnerRequestDto.setPartnerReferenceNumber( entity.getPartnerReferenceNumber() );
        partnerRequestDto.setPartnerStatus( entity.isPartnerStatus() );
        partnerRequestDto.setPartnerType( entity.getPartnerType() );
        partnerRequestDto.setPaymentNature( entity.getPaymentNature() );
        partnerRequestDto.setReimbursement( entity.getReimbursement() );
        partnerRequestDto.setSubscriptionBillingAccount( entity.getSubscriptionBillingAccount() );
        partnerRequestDto.setSubscriptionFee( entity.getSubscriptionFee() );
        partnerRequestDto.setSubscriptionPeriodicity( entity.getSubscriptionPeriodicity() );
        partnerRequestDto.setValidDaysMassCashPickupTransfer( entity.getValidDaysMassCashPickupTransfer() );

        return partnerRequestDto;
    }

    @Override
    public PartnerResponseDto toResponseDto(Partner entity) {
        if ( entity == null ) {
            return null;
        }

        PartnerResponseDto partnerResponseDto = new PartnerResponseDto();

        partnerResponseDto.setName( entity.getName() );
        partnerResponseDto.setPartnerReferenceNumber( entity.getPartnerReferenceNumber() );
        partnerResponseDto.setPartnerStatus( entity.isPartnerStatus() );
        partnerResponseDto.setPartnerType( entity.getPartnerType() );

        return partnerResponseDto;
    }
}
