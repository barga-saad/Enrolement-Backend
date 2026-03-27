package org.bcp.dlmsenrolment.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransferRequestDto {

//    private Long id;

    // Identification
    private String endToEndId;
    private String partnerReferenceNumber;
    private String status;
    private String statusMessage;

    // Remittance
    private String remittanceInformation;
    private String operationType;

    // Amount
    private BigDecimal paymentAmount;
    private String paymentCurrency;

    // Debitor
    private String debitorFirstName;
    private String debitorMiddleName;
    private String debitorLastName;
    private String debitorRegistrationId;
    private String debitorIdType;
    private String debitorIdValue;
    private String debitorBirthDate;
    private String debitorBirthCountry;
    private String debitorAddressLine1;
    private String debitorAddressLine2;
    private String debitorAddressLine3;
    private String debitorAddressCity;
    private String debitorCountryCode;
    private String debitorAccountNumber;
    private String debitorAccountType;

    // Creditor
    private String creditorFirstName;
    private String creditorMiddleName;
    private String creditorLastName;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAddressLine1;
    private String creditorAddressLine2;
    private String creditorAddressLine3;
    private String creditorAddressCity;
    private String creditorCountryCode;

    // Account (Destinataire)
    private String accountNumber;
    private String accountType;

    // Account (Compte Correspondant partenaire)
    private String partnerAccountNumber;
    private String partnerAccountType;

    // Supplementary Data
    private String sourceOfFundsType;
    private String sourceOfFunds;
    private String customerRelationshipFlag;
    private BigDecimal commissionAmount;
    private String commissionCurrency;
    private BigDecimal fxReceivedAmount;
    private String fxReceivedCurrency;
    private BigDecimal fxExchangeRate;
    private String amlCheck;
    private String kycStatus;
    private String riskScore;

    // Country info
    private String senderCountryCode;
    private String receiverCountryCode;

    // Execution
    private String requestedExecutionDate;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transfer API Return Data
    private String internalPaymentReference;
    private String rejectionReason;
    private String rejectionInternalCode;
    private Double commission;
    private Double Tva;
    private String matricule;
    private String guichet;
    private String creationHour;
    private String beneficiaryName;
    private String requestOut;
    private String transferOut;

    private String errorCode;
    private String errorMessage;
    private String errorField;

    private Boolean isReplayed;
    private Integer replayCount;

    // Rejection / GSIMT
    private String gsimtStatus;
    private String gsimtRejectionCode;
    private String gsimtRejectionReason;
    private String rejectionReasonLabel;
    private String rejectionReasonCode;
    private String transferRejectionCode;

}
