package org.bcp.dlmsenrolment.mappers;

import javax.annotation.processing.Generated;
import org.bcp.dlmsenrolment.dtos.request.TransferRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.TransferResponseDto;
import org.bcp.dlmsenrolment.entities.Transfer;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-01T14:12:35+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TransferMapperImpl implements TransferMapper {

    @Override
    public TransferRequestDto toRequestDto(Transfer transfer) {
        if ( transfer == null ) {
            return null;
        }

        TransferRequestDto transferRequestDto = new TransferRequestDto();

        transferRequestDto.setAccountNumber( transfer.getAccountNumber() );
        transferRequestDto.setAccountType( transfer.getAccountType() );
        transferRequestDto.setAmlCheck( transfer.getAmlCheck() );
        transferRequestDto.setBeneficiaryName( transfer.getBeneficiaryName() );
        transferRequestDto.setCommission( transfer.getCommission() );
        transferRequestDto.setCommissionAmount( transfer.getCommissionAmount() );
        transferRequestDto.setCommissionCurrency( transfer.getCommissionCurrency() );
        transferRequestDto.setCreatedAt( transfer.getCreatedAt() );
        transferRequestDto.setCreationHour( transfer.getCreationHour() );
        transferRequestDto.setCreditorAddressCity( transfer.getCreditorAddressCity() );
        transferRequestDto.setCreditorAddressLine1( transfer.getCreditorAddressLine1() );
        transferRequestDto.setCreditorAddressLine2( transfer.getCreditorAddressLine2() );
        transferRequestDto.setCreditorAddressLine3( transfer.getCreditorAddressLine3() );
        transferRequestDto.setCreditorCountryCode( transfer.getCreditorCountryCode() );
        transferRequestDto.setCreditorFirstName( transfer.getCreditorFirstName() );
        transferRequestDto.setCreditorIdType( transfer.getCreditorIdType() );
        transferRequestDto.setCreditorIdValue( transfer.getCreditorIdValue() );
        transferRequestDto.setCreditorLastName( transfer.getCreditorLastName() );
        transferRequestDto.setCreditorMiddleName( transfer.getCreditorMiddleName() );
        transferRequestDto.setCustomerRelationshipFlag( transfer.getCustomerRelationshipFlag() );
        transferRequestDto.setDebitorAccountNumber( transfer.getDebitorAccountNumber() );
        transferRequestDto.setDebitorAccountType( transfer.getDebitorAccountType() );
        transferRequestDto.setDebitorAddressCity( transfer.getDebitorAddressCity() );
        transferRequestDto.setDebitorAddressLine1( transfer.getDebitorAddressLine1() );
        transferRequestDto.setDebitorAddressLine2( transfer.getDebitorAddressLine2() );
        transferRequestDto.setDebitorAddressLine3( transfer.getDebitorAddressLine3() );
        transferRequestDto.setDebitorBirthCountry( transfer.getDebitorBirthCountry() );
        transferRequestDto.setDebitorBirthDate( transfer.getDebitorBirthDate() );
        transferRequestDto.setDebitorCountryCode( transfer.getDebitorCountryCode() );
        transferRequestDto.setDebitorFirstName( transfer.getDebitorFirstName() );
        transferRequestDto.setDebitorIdType( transfer.getDebitorIdType() );
        transferRequestDto.setDebitorIdValue( transfer.getDebitorIdValue() );
        transferRequestDto.setDebitorLastName( transfer.getDebitorLastName() );
        transferRequestDto.setDebitorMiddleName( transfer.getDebitorMiddleName() );
        transferRequestDto.setDebitorRegistrationId( transfer.getDebitorRegistrationId() );
        transferRequestDto.setEndToEndId( transfer.getEndToEndId() );
        transferRequestDto.setErrorCode( transfer.getErrorCode() );
        transferRequestDto.setErrorField( transfer.getErrorField() );
        transferRequestDto.setErrorMessage( transfer.getErrorMessage() );
        transferRequestDto.setFxExchangeRate( transfer.getFxExchangeRate() );
        transferRequestDto.setFxReceivedAmount( transfer.getFxReceivedAmount() );
        transferRequestDto.setFxReceivedCurrency( transfer.getFxReceivedCurrency() );
        transferRequestDto.setGsimtRejectionCode( transfer.getGsimtRejectionCode() );
        transferRequestDto.setGsimtRejectionReason( transfer.getGsimtRejectionReason() );
        transferRequestDto.setGsimtStatus( transfer.getGsimtStatus() );
        transferRequestDto.setGuichet( transfer.getGuichet() );
        transferRequestDto.setInternalPaymentReference( transfer.getInternalPaymentReference() );
        transferRequestDto.setIsReplayed( transfer.getIsReplayed() );
        transferRequestDto.setKycStatus( transfer.getKycStatus() );
        transferRequestDto.setMatricule( transfer.getMatricule() );
        transferRequestDto.setOperationType( transfer.getOperationType() );
        transferRequestDto.setPartnerAccountNumber( transfer.getPartnerAccountNumber() );
        transferRequestDto.setPartnerAccountType( transfer.getPartnerAccountType() );
        transferRequestDto.setPartnerReferenceNumber( transfer.getPartnerReferenceNumber() );
        transferRequestDto.setPaymentAmount( transfer.getPaymentAmount() );
        transferRequestDto.setPaymentCurrency( transfer.getPaymentCurrency() );
        transferRequestDto.setReceiverCountryCode( transfer.getReceiverCountryCode() );
        transferRequestDto.setRejectionInternalCode( transfer.getRejectionInternalCode() );
        transferRequestDto.setRejectionReason( transfer.getRejectionReason() );
        transferRequestDto.setRejectionReasonCode( transfer.getRejectionReasonCode() );
        transferRequestDto.setRejectionReasonLabel( transfer.getRejectionReasonLabel() );
        transferRequestDto.setRemittanceInformation( transfer.getRemittanceInformation() );
        transferRequestDto.setReplayCount( transfer.getReplayCount() );
        transferRequestDto.setRequestOut( transfer.getRequestOut() );
        transferRequestDto.setRequestedExecutionDate( transfer.getRequestedExecutionDate() );
        transferRequestDto.setRiskScore( transfer.getRiskScore() );
        transferRequestDto.setSenderCountryCode( transfer.getSenderCountryCode() );
        transferRequestDto.setSourceOfFunds( transfer.getSourceOfFunds() );
        transferRequestDto.setSourceOfFundsType( transfer.getSourceOfFundsType() );
        transferRequestDto.setStatus( transfer.getStatus() );
        transferRequestDto.setStatusMessage( transfer.getStatusMessage() );
        transferRequestDto.setTransferOut( transfer.getTransferOut() );
        transferRequestDto.setTransferRejectionCode( transfer.getTransferRejectionCode() );
        transferRequestDto.setTva( transfer.getTva() );
        transferRequestDto.setUpdatedAt( transfer.getUpdatedAt() );

        return transferRequestDto;
    }

    @Override
    public TransferResponseDto toResponseDto(Transfer transfer) {
        if ( transfer == null ) {
            return null;
        }

        TransferResponseDto transferResponseDto = new TransferResponseDto();

        transferResponseDto.setAccountNumber( transfer.getAccountNumber() );
        transferResponseDto.setAccountType( transfer.getAccountType() );
        transferResponseDto.setAmlCheck( transfer.getAmlCheck() );
        transferResponseDto.setBeneficiaryName( transfer.getBeneficiaryName() );
        transferResponseDto.setCommission( transfer.getCommission() );
        transferResponseDto.setCommissionAmount( transfer.getCommissionAmount() );
        transferResponseDto.setCommissionCurrency( transfer.getCommissionCurrency() );
        transferResponseDto.setCreatedAt( transfer.getCreatedAt() );
        transferResponseDto.setCreationHour( transfer.getCreationHour() );
        transferResponseDto.setCreditorAddressCity( transfer.getCreditorAddressCity() );
        transferResponseDto.setCreditorAddressLine1( transfer.getCreditorAddressLine1() );
        transferResponseDto.setCreditorAddressLine2( transfer.getCreditorAddressLine2() );
        transferResponseDto.setCreditorAddressLine3( transfer.getCreditorAddressLine3() );
        transferResponseDto.setCreditorCountryCode( transfer.getCreditorCountryCode() );
        transferResponseDto.setCreditorFirstName( transfer.getCreditorFirstName() );
        transferResponseDto.setCreditorIdType( transfer.getCreditorIdType() );
        transferResponseDto.setCreditorIdValue( transfer.getCreditorIdValue() );
        transferResponseDto.setCreditorLastName( transfer.getCreditorLastName() );
        transferResponseDto.setCreditorMiddleName( transfer.getCreditorMiddleName() );
        transferResponseDto.setCustomerRelationshipFlag( transfer.getCustomerRelationshipFlag() );
        transferResponseDto.setDebitorAccountNumber( transfer.getDebitorAccountNumber() );
        transferResponseDto.setDebitorAccountType( transfer.getDebitorAccountType() );
        transferResponseDto.setDebitorAddressCity( transfer.getDebitorAddressCity() );
        transferResponseDto.setDebitorAddressLine1( transfer.getDebitorAddressLine1() );
        transferResponseDto.setDebitorAddressLine2( transfer.getDebitorAddressLine2() );
        transferResponseDto.setDebitorAddressLine3( transfer.getDebitorAddressLine3() );
        transferResponseDto.setDebitorBirthCountry( transfer.getDebitorBirthCountry() );
        transferResponseDto.setDebitorBirthDate( transfer.getDebitorBirthDate() );
        transferResponseDto.setDebitorCountryCode( transfer.getDebitorCountryCode() );
        transferResponseDto.setDebitorFirstName( transfer.getDebitorFirstName() );
        transferResponseDto.setDebitorIdType( transfer.getDebitorIdType() );
        transferResponseDto.setDebitorIdValue( transfer.getDebitorIdValue() );
        transferResponseDto.setDebitorLastName( transfer.getDebitorLastName() );
        transferResponseDto.setDebitorMiddleName( transfer.getDebitorMiddleName() );
        transferResponseDto.setDebitorRegistrationId( transfer.getDebitorRegistrationId() );
        transferResponseDto.setEndToEndId( transfer.getEndToEndId() );
        transferResponseDto.setErrorCode( transfer.getErrorCode() );
        transferResponseDto.setErrorField( transfer.getErrorField() );
        transferResponseDto.setErrorMessage( transfer.getErrorMessage() );
        transferResponseDto.setFxExchangeRate( transfer.getFxExchangeRate() );
        transferResponseDto.setFxReceivedAmount( transfer.getFxReceivedAmount() );
        transferResponseDto.setFxReceivedCurrency( transfer.getFxReceivedCurrency() );
        transferResponseDto.setGsimtRejectionCode( transfer.getGsimtRejectionCode() );
        transferResponseDto.setGsimtRejectionReason( transfer.getGsimtRejectionReason() );
        transferResponseDto.setGsimtStatus( transfer.getGsimtStatus() );
        transferResponseDto.setGuichet( transfer.getGuichet() );
        transferResponseDto.setInternalPaymentReference( transfer.getInternalPaymentReference() );
        transferResponseDto.setIsReplayed( transfer.getIsReplayed() );
        transferResponseDto.setKycStatus( transfer.getKycStatus() );
        transferResponseDto.setMatricule( transfer.getMatricule() );
        transferResponseDto.setOperationType( transfer.getOperationType() );
        transferResponseDto.setPartnerAccountNumber( transfer.getPartnerAccountNumber() );
        transferResponseDto.setPartnerAccountType( transfer.getPartnerAccountType() );
        transferResponseDto.setPartnerReferenceNumber( transfer.getPartnerReferenceNumber() );
        transferResponseDto.setPaymentAmount( transfer.getPaymentAmount() );
        transferResponseDto.setPaymentCurrency( transfer.getPaymentCurrency() );
        transferResponseDto.setReceiverCountryCode( transfer.getReceiverCountryCode() );
        transferResponseDto.setRejectionInternalCode( transfer.getRejectionInternalCode() );
        transferResponseDto.setRejectionReason( transfer.getRejectionReason() );
        transferResponseDto.setRejectionReasonCode( transfer.getRejectionReasonCode() );
        transferResponseDto.setRejectionReasonLabel( transfer.getRejectionReasonLabel() );
        transferResponseDto.setRemittanceInformation( transfer.getRemittanceInformation() );
        transferResponseDto.setReplayCount( transfer.getReplayCount() );
        transferResponseDto.setRequestOut( transfer.getRequestOut() );
        transferResponseDto.setRequestedExecutionDate( transfer.getRequestedExecutionDate() );
        transferResponseDto.setRiskScore( transfer.getRiskScore() );
        transferResponseDto.setSenderCountryCode( transfer.getSenderCountryCode() );
        transferResponseDto.setSourceOfFunds( transfer.getSourceOfFunds() );
        transferResponseDto.setSourceOfFundsType( transfer.getSourceOfFundsType() );
        transferResponseDto.setStatus( transfer.getStatus() );
        transferResponseDto.setStatusMessage( transfer.getStatusMessage() );
        transferResponseDto.setTransferOut( transfer.getTransferOut() );
        transferResponseDto.setTransferRejectionCode( transfer.getTransferRejectionCode() );
        transferResponseDto.setTva( transfer.getTva() );
        transferResponseDto.setUpdatedAt( transfer.getUpdatedAt() );

        return transferResponseDto;
    }
}
