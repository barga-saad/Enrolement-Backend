package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identification
    private String endToEndId;
    private String partnerReferenceNumber;

    @Column(name = "status_code", nullable = false)
    private String status;

    @Column(name = "status_message", nullable = false)
    private String statusMessage;

    // Remittance
    @Column(length = 140)
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
    @Column(name = "creditor_account_number", length = 24)
    private String accountNumber;

    @Column(name = "creditor_account_number_16", length = 16)
    private String accountNumber16;

    @Column(name = "creditor_account_type", length = 35)
    private String accountType;

    // Account (Compte Correspondant partenaire)
    @Column(name = "partner_account_number", length = 24)
    private String partnerAccountNumber;
    @Column(name = "partner_account_type", length = 35)
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
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    //Transfer API Return Data
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

    @Column(name = "error_code", length = 10)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_field")
    private String errorField;

    @Column(name = "is_replayed")
    private Boolean isReplayed = false;

    @Column(name = "replay_count")
    private Integer replayCount = 0;
    // Rejection / GSIMT
    private String gsimtStatus;              // statutGSIMT
    private String gsimtRejectionCode;       // codeMotifRejetGSIMT
    private String gsimtRejectionReason;     // libelleMotifRejetGSIMT
    private String rejectionReasonLabel;     // libelleMotifRejetVirement
    private String rejectionReasonCode;      // codeMotifRejetVirement
    private String transferRejectionCode;    // codeRejetVirement
    @Column(name = "is_reconciled")
    private Boolean isReconciled = false;
}
