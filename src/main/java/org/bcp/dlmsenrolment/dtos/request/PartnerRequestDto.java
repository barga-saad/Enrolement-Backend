package org.bcp.dlmsenrolment.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class PartnerRequestDto {

    private String name;
    private String partnerReferenceNumber;
    private String partnerType;
    private boolean partnerStatus;
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

    // Transfer limits — default 0 so NOT NULL constraint is satisfied
    private BigDecimal limitInternalInstantTransfer = BigDecimal.ZERO;
    private BigDecimal limitExternalInstantTransfer = BigDecimal.ZERO;
    private BigDecimal limitStandardTransfer = BigDecimal.ZERO;
    private BigDecimal limitCashPickupTransfer = BigDecimal.ZERO;
    private BigDecimal limitMassCashPickupTransfer = BigDecimal.ZERO;
    private BigDecimal limitWalletTransfer = BigDecimal.ZERO;

    private String natureMassCashPickupTransfer = "0";
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

    private String paymentNature = "Post-paiement";

    // Tariff conditions
    private String feeMode;
    private Boolean monthlyBilling = false;
    private Boolean immediateDebit = false;
    private String reimbursement;

    // Commission currency
    private String commissionCurrency = "MAD";

    // Commissions — Instant Internal
    private String commissionTypeInstantInternal = "Flat";
    private BigDecimal commissionAmountInstantInternal = BigDecimal.ZERO;
    private BigDecimal commissionRateInstantInternal = BigDecimal.ZERO;
    private BigDecimal commissionMinInstantInternal;
    private BigDecimal commissionMaxInstantInternal;

    // Commissions — Instant External
    private String commissionTypeInstantExternal = "Flat";
    private BigDecimal commissionAmountInstantExternal = BigDecimal.ZERO;
    private BigDecimal commissionRateInstantExternal = BigDecimal.ZERO;
    private BigDecimal commissionMinInstantExternal;
    private BigDecimal commissionMaxInstantExternal;

    // Commissions — Standard External
    private String commissionTypeStandardExternal = "Flat";
    private BigDecimal commissionAmountStandardExternal = BigDecimal.ZERO;
    private BigDecimal commissionRateStandardExternal = BigDecimal.ZERO;
    private BigDecimal commissionMinStandardExternal;
    private BigDecimal commissionMaxStandardExternal;

    // Commissions — Mass Cash Pickup
    private String commissionTypeMassCashPickup = "Flat";
    private BigDecimal commissionAmountMassCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionRateMassCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionMinMassCashPickup;
    private BigDecimal commissionMaxMassCashPickup;

    // Commissions — Cash Pickup
    private String commissionTypeCashPickup = "Flat";
    private BigDecimal commissionAmountCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionRateCashPickup = BigDecimal.ZERO;
    private BigDecimal commissionMinCashPickup;
    private BigDecimal commissionMaxCashPickup;

    // Commissions — Wallet
    private String commissionTypeWallet = "Flat";
    private BigDecimal commissionAmountWallet = BigDecimal.ZERO;
    private BigDecimal commissionRateWallet = BigDecimal.ZERO;
    private BigDecimal commissionMinWallet;
    private BigDecimal commissionMaxWallet;
}
