package org.bcp.dlmsenrolment.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class PartnerRequestDto {
    // You can uncomment id if you want it in responses/updates
    // private Long id;

    private String name;
    private String partnerReferenceNumber;
    private String partnerType;
    private boolean partnerStatus;
    private String headquarterCountry;
    private String parentCompanyCountry;
    private String classification;
    private String accountNumber;
    private String canal;

    // Transfer permissions
    private boolean allowedInternalInstantTransfer;
    private boolean allowedExternalInstantTransfer;
    private boolean allowedStandardTransfer;
    private boolean allowedCashPickupTransfer;
    private boolean allowedMassCashPickupTransfer;
    private boolean allowedWalletTransfer;

    // Transfer limits
    private BigDecimal limitInternalInstantTransfer;
    private BigDecimal limitExternalInstantTransfer;
    private BigDecimal limitStandardTransfer;
    private BigDecimal limitCashPickupTransfer;
    private BigDecimal limitMassCashPickupTransfer;
    private BigDecimal limitWalletTransfer;

    // ⚠️ entity = String -> was BigDecimal in your DTO
    private String natureMassCashPickupTransfer;

    private Integer validDaysMassCashPickupTransfer;

    // Currency
    private String mainCurrency;

    // Exchange
    private String exchangeRateNature;
    private BigDecimal exchangeGainPercentage;

    // Subscription
    private BigDecimal subscriptionFee;
    private String subscriptionPeriodicity;

    // ⚠️ entity = String -> was BigDecimal in your DTO
    private String subscriptionBillingAccount;

    private String paymentNature; // Post-paiement / Prépaiement

    // Tariff conditions
    private String feeMode;
    private Boolean monthlyBilling;
    private Boolean immediateDebit;

    // ⚠️ entity = String -> was Boolean in your DTO
    private String reimbursement; // reversement

    // Commission currency (non-null in entity)
    private String commissionCurrency;

    // Commissions — Instant Internal
    private String commissionTypeInstantInternal;
    private BigDecimal commissionAmountInstantInternal;
    private BigDecimal commissionRateInstantInternal;
    private BigDecimal commissionMinInstantInternal;
    private BigDecimal commissionMaxInstantInternal;

    // Commissions — Instant External
    private String commissionTypeInstantExternal;
    private BigDecimal commissionAmountInstantExternal;
    private BigDecimal commissionRateInstantExternal;
    private BigDecimal commissionMinInstantExternal;
    private BigDecimal commissionMaxInstantExternal;

    // Commissions — Standard External
    private String commissionTypeStandardExternal;
    private BigDecimal commissionAmountStandardExternal;
    private BigDecimal commissionRateStandardExternal;
    private BigDecimal commissionMinStandardExternal;
    private BigDecimal commissionMaxStandardExternal;

    // Commissions — Mass Cash Pickup
    private String commissionTypeMassCashPickup;
    private BigDecimal commissionAmountMassCashPickup;
    private BigDecimal commissionRateMassCashPickup;
    private BigDecimal commissionMinMassCashPickup;
    private BigDecimal commissionMaxMassCashPickup;

    // Commissions — Cash Pickup
    private String commissionTypeCashPickup;
    private BigDecimal commissionAmountCashPickup;
    private BigDecimal commissionRateCashPickup;
    private BigDecimal commissionMinCashPickup;
    private BigDecimal commissionMaxCashPickup;

    // Commissions — Wallet
    private String commissionTypeWallet;
    private BigDecimal commissionAmountWallet;
    private BigDecimal commissionRateWallet;
    private BigDecimal commissionMinWallet;
    private BigDecimal commissionMaxWallet;
}
