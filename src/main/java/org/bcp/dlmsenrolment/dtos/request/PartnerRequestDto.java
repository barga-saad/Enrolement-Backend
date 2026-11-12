package org.bcp.dlmsenrolment.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import org.bcp.dlmsenrolment.enums.AccountingType;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerRequestDto {

    private String name;
    private String partnerReferenceNumber;
    private String partnerType;
    private String partnerStatus;
    private String headquarterCountry;
    private String parentCompanyCountry;
    private String classification;
    private String accountNumber;
    private String canal;

    // Transfer permissions (boolean primitives default to false)
    private boolean allowedInternalInstantTransfer;
    private boolean allowedExternalInstantTransfer;
    private boolean allowedStandardTransfer;
    private boolean allowedCashPickupTransfer;
    private boolean allowedMassCashPickupTransfer;
    private boolean allowedWalletTransfer;
    private boolean allowedSrbmRtgsTransfer;

    // Transfer limits - default 0 so NOT NULL constraint is satisfied
    private BigDecimal limitInternalInstantTransfer = BigDecimal.ZERO;
    private BigDecimal limitExternalInstantTransfer = BigDecimal.ZERO;
    private BigDecimal limitStandardTransfer = BigDecimal.ZERO;
    private BigDecimal limitCashPickupTransfer = BigDecimal.ZERO;
    private BigDecimal limitMassCashPickupTransfer = BigDecimal.ZERO;
    private BigDecimal limitWalletTransfer = BigDecimal.ZERO;
    private BigDecimal limitSrbmRtgsTransfer = BigDecimal.ZERO;

    private String natureMassCashPickupTransfer;
    private Integer validDaysMassCashPickupTransfer = 0;

    // Currency
    private String mainCurrency = "MAD";

    // Exchange
    private String exchangeRateNature = "0";
    private BigDecimal exchangeGainPercentage = BigDecimal.ZERO;

    // Subscription
    private BigDecimal subscriptionFee = BigDecimal.ZERO;
    private String subscriptionPeriodicity;
    private String subscriptionBillingAccount;

    private String paymentNature = "POST_PAYMENT";
    private AccountingType accountingType;

    // Tariff conditions
    private String feeMode;
    private Boolean monthlyBilling = false;
    private Boolean immediateDebit = false;
    private Boolean reimbursement;

    // Commission currency
    private String commissionCurrency = "MAD";

    // Commissions - Instant Internal
    private String commissionTypeInstantInternal = "FLAT";
    private BigDecimal commissionAmountInstantInternal = BigDecimal.ZERO;
    private BigDecimal commissionRateInstantInternal = BigDecimal.ZERO;
    private BigDecimal commissionMinInstantInternal;
    private BigDecimal commissionMaxInstantInternal;

    // Commissions - Instant External
    private String commissionTypeInstantExternal = "FLAT";
    private BigDecimal commissionAmountInstantExternal = BigDecimal.ZERO;
    private BigDecimal commissionRateInstantExternal = BigDecimal.ZERO;
    private BigDecimal commissionMinInstantExternal;
    private BigDecimal commissionMaxInstantExternal;

    // Commissions - Standard External
    private String commissionTypeStandardExternal = "FLAT";
    private BigDecimal commissionAmountStandardExternal = BigDecimal.ZERO;
    private BigDecimal commissionRateStandardExternal = BigDecimal.ZERO;
    private BigDecimal commissionMinStandardExternal;
    private BigDecimal commissionMaxStandardExternal;

    // Commissions - Mass Cash Pickup
    private String commissionTypeMassCashPickup = "FLAT";
    private BigDecimal commissionAmountMassCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionRateMassCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionMinMassCashPickup;
    private BigDecimal commissionMaxMassCashPickup;

    // Commissions - Cash Pickup
    private String commissionTypeCashPickup = "FLAT";
    private BigDecimal commissionAmountCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionRateCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionMinCashPickup;
    private BigDecimal commissionMaxCashPickup;

    // Commissions - Wallet
    private String commissionTypeWallet = "FLAT";
    private BigDecimal commissionAmountWallet = BigDecimal.ZERO;
    private BigDecimal commissionRateWallet = BigDecimal.ZERO;
    private BigDecimal commissionMinWallet;
    private BigDecimal commissionMaxWallet;

    private String commissionTypeSrbmRtgs = "FLAT";
    private BigDecimal commissionAmountSrbmRtgs = BigDecimal.ZERO;
    private BigDecimal commissionRateSrbmRtgs = BigDecimal.ZERO;
    private BigDecimal commissionMinSrbmRtgs;
    private BigDecimal commissionMaxSrbmRtgs;

    private String commissionTypeForexReception = "PERCENTAGE";
    private BigDecimal commissionAmountForexReception = BigDecimal.ZERO;
    private BigDecimal commissionRateForexReception = BigDecimal.ZERO;
    private BigDecimal commissionMinForexReception;
    private BigDecimal commissionMaxForexReception;

    private String commissionTypeForexEmission = "PERCENTAGE";
    private BigDecimal commissionAmountForexEmission = BigDecimal.ZERO;
    private BigDecimal commissionRateForexEmission = BigDecimal.ZERO;
    private BigDecimal commissionMinForexEmission;
    private BigDecimal commissionMaxForexEmission;

    // ===== ENROLLMENT TYPE =====
    // "PARTNER" (default) or "CLIENT" - controls which form sections are active
    private String enrollmentType = "PARTNER";

    // ===== FOREX - RECEPTION (Partner only) =====
    private Boolean autoriserReception = false;
    private String devisesReception;           // e.g. "EUR,USD,GBP"
    private BigDecimal plafondReception;

    // ===== FOREX - EMISSION (Partner only) =====
    private Boolean autoriserEmission = false;
    private String typeTransactionEmission;    // e.g. "International"
    private Boolean autorisationOC = false;
    private String refAutorisationOC;
    private String devisesEmission;            // e.g. "EUR,USD"
    private String natureCoursChangeEmission;
    private BigDecimal plafondMontantDevise;

    // ===== CONFORMITE - BLOC A (Donneur d'ordre) =====
    private Boolean conformiteDOTypePiece = false;
    private Boolean conformiteDONumPiece = false;
    private Boolean conformiteDODateNaissance = false;
    private Boolean conformiteDOPaysNaissance = false;
    private Boolean conformiteDOAdresse = false;
    private Boolean conformiteDOVilleResidence = false;

    // ===== CONFORMITE - BLOC B (Beneficiaire) =====
    private Boolean conformiteBenefTypePiece = false;
    private Boolean conformiteBenefNumPiece = false;
    private Boolean conformiteBenefAdresse = false;
    private Boolean conformiteBenefVilleResidence = false;
    private Boolean conformiteBenefPaysResidence = false;

    // ===== FICHIER SYNTHESE =====
    private Boolean fichierSynthese = true;

    // ===== FACTURATION =====
    private BigDecimal setupFee;
    private Boolean setupFeeBilled = false;
    private String billingMode;           // PAR_TRANSACTION | MENSUELLE | LES_DEUX
    private Boolean derogation = false;
    private String derogationDetails;
}
