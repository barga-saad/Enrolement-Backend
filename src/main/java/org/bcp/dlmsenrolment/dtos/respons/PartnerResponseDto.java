package org.bcp.dlmsenrolment.dtos.respons;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PartnerResponseDto {

    // ===== IDENTITY =====
    private Long id;
    private String name;
    private String partnerReferenceNumber;
    private String partnerType;
    private String partnerStatus;
    private String enrollmentType;     // PARTNER | CLIENT
    private String workflowStatus;     // DRAFT, PENDING, VALIDATED, REJECTED
    private String headquarterCountry;
    private String parentCompanyCountry;
    private String classification;
    private String accountNumber;
    private String canal;

    // ===== AUDIT =====
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== TRANSFER PERMISSIONS =====
    private boolean allowedInternalInstantTransfer;
    private boolean allowedExternalInstantTransfer;
    private boolean allowedStandardTransfer;
    private boolean allowedCashPickupTransfer;
    private boolean allowedMassCashPickupTransfer;
    private boolean allowedWalletTransfer;

    // ===== TRANSFER LIMITS =====
    private BigDecimal limitInternalInstantTransfer;
    private BigDecimal limitExternalInstantTransfer;
    private BigDecimal limitStandardTransfer;
    private BigDecimal limitCashPickupTransfer;
    private BigDecimal limitMassCashPickupTransfer;
    private BigDecimal limitWalletTransfer;
    private BigDecimal natureMassCashPickupTransfer;
    private Integer validDaysMassCashPickupTransfer;

    // ===== CURRENCY & EXCHANGE =====
    private String mainCurrency;
    private String exchangeRateNature;
    private BigDecimal exchangeGainPercentage;

    // ===== SUBSCRIPTION =====
    private BigDecimal subscriptionFee;
    private String subscriptionPeriodicity;
    private BigDecimal subscriptionBillingAccount;
    private String paymentNature;

    // ===== TARIFF CONDITIONS =====
    private String feeMode;
    private Boolean monthlyBilling;
    private Boolean immediateDebit;
    private Boolean reimbursement;

    // ===== FOREX - RECEPTION =====
    private Boolean autoriserReception;
    private String devisesReception;
    private BigDecimal plafondReception;

    // ===== FOREX - EMISSION =====
    private Boolean autoriserEmission;
    private String typeTransactionEmission;
    private Boolean autorisationOC;
    private String refAutorisationOC;
    private String devisesEmission;
    private String natureCoursChangeEmission;
    private BigDecimal plafondMontantDevise;

    // ===== CONFORMITE - BLOC A =====
    private Boolean conformiteDOTypePiece;
    private Boolean conformiteDONumPiece;
    private Boolean conformiteDODateNaissance;
    private Boolean conformiteDOPaysNaissance;
    private Boolean conformiteDOAdresse;
    private Boolean conformiteDOVilleResidence;

    // ===== CONFORMITE - BLOC B =====
    private Boolean conformiteBenefTypePiece;
    private Boolean conformiteBenefNumPiece;
    private Boolean conformiteBenefAdresse;
    private Boolean conformiteBenefVilleResidence;
    private Boolean conformiteBenefPaysResidence;

    // ===== FICHIER SYNTHESE =====
    private Boolean fichierSynthese;

    // ===== FACTURATION =====
    private BigDecimal setupFee;
    private Boolean setupFeeBilled;
    private String billingMode;
    private Boolean derogation;
    private String derogationDetails;

    // ===== COMMISSIONS =====
    private String commissionCurrency;

    private String commissionTypeInstantInternal;
    private BigDecimal commissionAmountInstantInternal;
    private BigDecimal commissionRateInstantInternal;
    private BigDecimal commissionMinInstantInternal;
    private BigDecimal commissionMaxInstantInternal;

    private String commissionTypeInstantExternal;
    private BigDecimal commissionAmountInstantExternal;
    private BigDecimal commissionRateInstantExternal;
    private BigDecimal commissionMinInstantExternal;
    private BigDecimal commissionMaxInstantExternal;

    private String commissionTypeStandardExternal;
    private BigDecimal commissionAmountStandardExternal;
    private BigDecimal commissionRateStandardExternal;
    private BigDecimal commissionMinStandardExternal;
    private BigDecimal commissionMaxStandardExternal;

    private String commissionTypeMassCashPickup;
    private BigDecimal commissionAmountMassCashPickup;
    private BigDecimal commissionRateMassCashPickup;
    private BigDecimal commissionMinMassCashPickup;
    private BigDecimal commissionMaxMassCashPickup;

    private String commissionTypeCashPickup;
    private BigDecimal commissionAmountCashPickup;
    private BigDecimal commissionRateCashPickup;
    private BigDecimal commissionMinCashPickup;
    private BigDecimal commissionMaxCashPickup;

    private String commissionTypeWallet;
    private BigDecimal commissionAmountWallet;
    private BigDecimal commissionRateWallet;
    private BigDecimal commissionMinWallet;
    private BigDecimal commissionMaxWallet;
}

