package org.bcp.dlmsenrolment.entities;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "partners")
public class Partner {
    // info
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // partner name( ex: Western Union)

    @Column(unique = true, nullable = false, length = 35)
    private String partnerReferenceNumber;

    @Column(nullable = false)
    private String partnerType;

    @Column(nullable = false)
    private String partnerStatus;

    @Column(name = "headquarter_country", nullable = false)
    private String headquarterCountry; // location of the main office of the partner (ex: FR, USA ...)

    @Column(name = "parent_company_country")
    private String parentCompanyCountry;

    @Column(name = "classification", nullable = false)
    private String classification; // Local / International

    @Column(name = "account_number", nullable = false, length = 16)
    private String accountNumber; // partner account number inside bcp

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PartnerCurrency> currencies = new HashSet<>();

    private String canal; //partner name in web service virement instantané needed to be add in every request to web service virement instantané

    // Transfer limits
    // permission for partner what type of transfer can do
    @Column(name = "allowed_internal_instant_transfer", nullable = false)
    private boolean allowedInternalInstantTransfer; // transfert instantane interne

    @Column(name = "allowed_external_instant_transfer", nullable = false)
    private boolean allowedExternalInstantTransfer; // transfert instantane confrere

    @Column(name = "allowed_standard_transfer", nullable = false)
    private boolean allowedStandardTransfer; // transfert confrere standard

    @Column(name = "allowed_cash_pickup_transfer", nullable = false)
    private boolean allowedCashPickupTransfer; // MAD Unitaire

    @Column(name = "allowed_mass_cash_pickup_transfer", nullable = false)
    private boolean allowedMassCashPickupTransfer; // MAD de masse

    @Column(name = "allowed_wallet_transfer", nullable = false)
    private boolean allowedWalletTransfer; // wallet
    // limit for partner what amount can he send in one transfer
    @Column(name = "limit_internal_instant_transfer", nullable = false)
    private BigDecimal limitInternalInstantTransfer;

    @Column(name = "limit_external_instant_transfer", nullable = false)
    private BigDecimal limitExternalInstantTransfer;

    @Column(name = "limit_standard_transfer", nullable = false)
    private BigDecimal limitStandardTransfer;

    @Column(name = "limit_cash_pickup_transfer", nullable = false)
    private BigDecimal limitCashPickupTransfer;

    @Column(name = "limit_mass_cash_pickup_transfer", nullable = false)
    private BigDecimal limitMassCashPickupTransfer;

    @Column(name = "limit_wallet_transfer", nullable = false)
    private BigDecimal limitWalletTransfer;

    @Column(name = "nature_mass_cash_pickup_transfer", nullable = false)
    private BigDecimal natureMassCashPickupTransfer;

    @Column(name = "valid_days_mass_cash_pickup_transfer", nullable = false)
    private Integer validDaysMassCashPickupTransfer;

    // Currency need to remove
    @Column(name = "main_currency", nullable = false)
    private String mainCurrency;

    // exchange
    @Column(name = "exchange_rate_nature")
    private String exchangeRateNature; // need to be explained

    @Column(name = "exchange_gain_percentage")
    private BigDecimal exchangeGainPercentage;

    // Subscription fees
    @Column(name = "subscription_fee")
    private BigDecimal subscriptionFee;

    @Column(name = "subscription_periodicity")
    private String subscriptionPeriodicity;

    @Column(name = "subscription_billing_account")
    private BigDecimal subscriptionBillingAccount;

    @Column(name = "payment_nature",nullable = false)
    private String paymentNature; // Post-paiement / Prépaiement

    // Tariff Conditions
    @Column(name = "fee_mode")
    private String feeMode; // Partage des frais ("OUR" : Sender pays all fees, "BEN" : Receiver pays all fees, "SHA" : Fees are shared between sender and receiver)

    @Column(name = "monthly_billing")
    private Boolean monthlyBilling; // Facturation mensuelle

    @Column(name = "immediate_debit")
    private Boolean immediateDebit; // Debit immediat

    @Column(name = "reimbursement")
    private Boolean reimbursement; //reversement

    //commission for every type of transfer
    @Column(name = "commission_currency",nullable = false)
    private String commissionCurrency;

    @Column(name = "commission_type_instant_internal",nullable = false)
    private String commissionTypeInstantInternal;

    @Column(name = "commission_amount_instant_internal",nullable = false)
    private BigDecimal commissionAmountInstantInternal;

    @Column(name = "commission_rate_instant_internal",nullable = false)
    private BigDecimal commissionRateInstantInternal;

    @Column(name = "commission_min_instant_internal")
    private BigDecimal commissionMinInstantInternal;

    @Column(name = "commission_max_instant_internal")
    private BigDecimal commissionMaxInstantInternal;

    @Column(name = "commission_type_instant_external",nullable = false)
    private String commissionTypeInstantExternal;

    @Column(name = "commission_amount_instant_external",nullable = false)
    private BigDecimal commissionAmountInstantExternal;

    @Column(name = "commission_rate_instant_external",nullable = false)
    private BigDecimal commissionRateInstantExternal;

    @Column(name = "commission_min_instant_external")
    private BigDecimal commissionMinInstantExternal;

    @Column(name = "commission_max_instant_external")
    private BigDecimal commissionMaxInstantExternal;

    @Column(name = "commission_type_standard_external",nullable = false)
    private String commissionTypeStandardExternal;

    @Column(name = "commission_amount_standard_external",nullable = false)
    private BigDecimal commissionAmountStandardExternal;

    @Column(name = "commission_rate_standard_external",nullable = false)
    private BigDecimal commissionRateStandardExternal;

    @Column(name = "commission_min_standard_external")
    private BigDecimal commissionMinStandardExternal;

    @Column(name = "commission_max_standard_external")
    private BigDecimal commissionMaxStandardExternal;

    @Column(name = "commission_type_mass_cash_pickup",nullable = false)
    private String commissionTypeMassCashPickup;

    @Column(name = "commission_amount_mass_cash_pickup",nullable = false)
    private BigDecimal commissionAmountMassCashPickup;

    @Column(name = "commission_rate_mass_cash_pickup",nullable = false)
    private BigDecimal commissionRateMassCashPickup;

    @Column(name = "commission_min_mass_cash_pickup")
    private BigDecimal commissionMinMassCashPickup;

    @Column(name = "commission_max_mass_cash_pickup")
    private BigDecimal commissionMaxMassCashPickup;

    @Column(name = "commission_type_cash_pickup",nullable = false)
    private String commissionTypeCashPickup;

    @Column(name = "commission_amount_cash_pickup",nullable = false)
    private BigDecimal commissionAmountCashPickup;

    @Column(name = "commission_rate_cash_pickup",nullable = false)
    private BigDecimal commissionRateCashPickup;

    @Column(name = "commission_min_cash_pickup")
    private BigDecimal commissionMinCashPickup;

    @Column(name = "commission_max_cash_pickup")
    private BigDecimal commissionMaxCashPickup;

    @Column(name = "commission_type_wallet",nullable = false)
    private String commissionTypeWallet;

    @Column(name = "commission_amount_wallet",nullable = false)
    private BigDecimal commissionAmountWallet;

    @Column(name = "commission_rate_wallet",nullable = false)
    private BigDecimal commissionRateWallet;

    @Column(name = "commission_min_wallet")
    private BigDecimal commissionMinWallet;

    @Column(name = "commission_max_wallet")
    private BigDecimal commissionMaxWallet;

    // ===== ENROLLMENT TYPE =====
    // columnDefinition with DEFAULT ensures existing rows are backfilled when Hibernate adds the column via ddl-auto=update
    @Column(name = "enrollment_type", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'PARTNER'")
    private String enrollmentType = "PARTNER"; // PARTNER | CLIENT

    // ===== WORKFLOW STATUS (denormalized for fast queries) =====
    @Column(name = "workflow_status", length = 20,
            columnDefinition = "varchar(20) default 'DRAFT'")
    private String workflowStatus = "DRAFT"; // DRAFT, PENDING, VALIDATED, REJECTED

    // ===== AUDIT FIELDS =====
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== FOREX - RECEPTION =====
    @Column(name = "autoriser_reception")
    private Boolean autoriserReception = false;

    @Column(name = "devises_reception", length = 200)
    private String devisesReception;

    @Column(name = "plafond_reception", precision = 18, scale = 2)
    private BigDecimal plafondReception;

    // ===== FOREX - EMISSION =====
    @Column(name = "autoriser_emission")
    private Boolean autoriserEmission = false;

    @Column(name = "type_transaction_emission", length = 100)
    private String typeTransactionEmission;

    @Column(name = "autorisation_oc")
    private Boolean autorisationOC = false;

    @Column(name = "ref_autorisation_oc", length = 100)
    private String refAutorisationOC;

    @Column(name = "devises_emission", length = 200)
    private String devisesEmission;

    @Column(name = "nature_cours_change_emission", length = 100)
    private String natureCoursChangeEmission;

    @Column(name = "plafond_montant_devise", precision = 18, scale = 2)
    private BigDecimal plafondMontantDevise;

    // ===== CONFORMITE - BLOC A (Donneur d'ordre) =====
    @Column(name = "conformite_do_type_piece")
    private Boolean conformiteDOTypePiece = false;

    @Column(name = "conformite_do_num_piece")
    private Boolean conformiteDONumPiece = false;

    @Column(name = "conformite_do_ville_residence")
    private Boolean conformiteDOVilleResidence = false;

    // ===== CONFORMITE - BLOC B (Beneficiaire) =====
    @Column(name = "conformite_benef_type_piece")
    private Boolean conformiteBenefTypePiece = false;

    @Column(name = "conformite_benef_num_piece")
    private Boolean conformiteBenefNumPiece = false;

    @Column(name = "conformite_benef_adresse")
    private Boolean conformiteBenefAdresse = false;

    // ===== FACTURATION =====
    @Column(name = "setup_fee", precision = 18, scale = 2)
    private BigDecimal setupFee;

    @Column(name = "setup_fee_billed")
    private Boolean setupFeeBilled = false;

    @Column(name = "billing_mode", length = 50)
    private String billingMode; // PAR_TRANSACTION | MENSUELLE | LES_DEUX

    @Column(name = "derogation")
    private Boolean derogation = false;

    @Column(name = "derogation_details", length = 500)
    private String derogationDetails;

}
