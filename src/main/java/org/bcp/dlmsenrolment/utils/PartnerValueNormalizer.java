package org.bcp.dlmsenrolment.utils;

import org.bcp.dlmsenrolment.entities.Partner;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class PartnerValueNormalizer {

    private PartnerValueNormalizer() {
    }

    public static void normalize(Partner partner) {
        partner.setPartnerType(map(partner.getPartnerType(), "PARTENAIRE", "PARTNER"));
        partner.setPartnerStatus(map(partner.getPartnerStatus(), "ACTIF", "ACTIVE", "INACTIF", "INACTIVE"));
        partner.setClassification(map(partner.getClassification(),
                "INTERNATIONNAL", "INTERNATIONAL", "INTERNATIONAL", "INTERNATIONAL"));
        partner.setHeadquarterCountry(country(partner.getHeadquarterCountry()));
        partner.setParentCompanyCountry(country(partner.getParentCompanyCountry()));
        partner.setMainCurrency(code(partner.getMainCurrency()));
        partner.setCommissionCurrency(code(partner.getCommissionCurrency()));
        partner.setExchangeRateNature(map(partner.getExchangeRateNature(), "NEGOCIE", "NEGOTIATED"));
        partner.setSubscriptionPeriodicity(map(partner.getSubscriptionPeriodicity(),
                "MENSUEL", "MONTHLY", "TRIMESTRIEL", "QUARTERLY", "SEMESTRIEL", "SEMIANNUAL", "ANNUEL", "ANNUAL"));
        partner.setPaymentNature(map(partner.getPaymentNature(),
            "OUI", "YES", "NON", "NO", "POST_PAIEMENT", "POST_PAYMENT", "POSTPAIEMENT", "POST_PAYMENT",
            "PRE_PAIEMENT", "PREPAYMENT", "PREPAIEMENT", "PREPAYMENT"));
        partner.setFeeMode(code(partner.getFeeMode()));
        partner.setEnrollmentType(code(partner.getEnrollmentType()));
        partner.setBillingMode(map(partner.getBillingMode(),
                "PAR_TRANSACTION", "PER_TRANSACTION", "MENSUELLE", "MONTHLY", "LES_DEUX", "BOTH"));
        partner.setTypeTransactionEmission(map(partner.getTypeTransactionEmission(),
                "VIREMENT_ORDINAIRE", "ORDINARY_TRANSFER", "AUTRE", "OTHER"));
        partner.setNatureCoursChangeEmission(map(partner.getNatureCoursChangeEmission(), "NEGOCIE", "NEGOTIATED"));
        partner.setDevisesReception(currencyList(partner.getDevisesReception()));
        partner.setDevisesEmission(currencyList(partner.getDevisesEmission()));

        partner.setCommissionTypeInstantInternal(commissionType(partner.getCommissionTypeInstantInternal()));
        partner.setCommissionTypeInstantExternal(commissionType(partner.getCommissionTypeInstantExternal()));
        partner.setCommissionTypeStandardExternal(commissionType(partner.getCommissionTypeStandardExternal()));
        partner.setCommissionTypeMassCashPickup(commissionType(partner.getCommissionTypeMassCashPickup()));
        partner.setCommissionTypeCashPickup(commissionType(partner.getCommissionTypeCashPickup()));
        partner.setCommissionTypeWallet(commissionType(partner.getCommissionTypeWallet()));
        partner.setCommissionTypeSrbmRtgs(commissionType(partner.getCommissionTypeSrbmRtgs()));
        partner.setCommissionTypeForexReception(commissionType(partner.getCommissionTypeForexReception()));
        partner.setCommissionTypeForexEmission(commissionType(partner.getCommissionTypeForexEmission()));
    }

    /**
     * True when the partner status means "active", including legacy French values ("Actif").
     */
    public static boolean isActive(String partnerStatus) {
        return "ACTIVE".equals(map(partnerStatus, "ACTIF", "ACTIVE"));
    }

    /**
     * RIB (24 caractères, chiffres ou lettres) = code banque (3) + code localité (3)
     * + numéro de compte (16) + clé RIB (2).
     * Stores the full RIB in {@code rib} and positions 7 to 22 in {@code accountNumber}.
     * Any other value (legacy data) is kept as-is in {@code accountNumber}.
     */
    public static void applyRib(Partner partner, String value) {
        if (value == null) {
            partner.setAccountNumber(null);
            return;
        }
        String trimmed = value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (trimmed.matches("[A-Z0-9]{24}")) {
            partner.setRib(trimmed);
            partner.setAccountNumber(trimmed.substring(6, 22));
        } else {
            partner.setAccountNumber(value);
        }
    }

    // Legacy rows may store a country name instead of its ISO code
    private static String country(String value) {
        return map(value, "FRANCE", "FR", "ETATS_UNIS", "US", "USA", "US", "MAROC", "MA");
    }

    private static String commissionType(String value) {
        return map(value, "FLAT", "FLAT", "POURCENTAGE", "PERCENTAGE");
    }

    private static String currencyList(String value) {
        if (value == null || value.isBlank()) return value;
        return Arrays.stream(value.split(","))
                .map(PartnerValueNormalizer::code)
                .collect(Collectors.joining(","));
    }

    private static String map(String value, String... pairs) {
        String normalized = code(value);
        if (normalized == null) return null;
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            if (normalized.equals(pairs[index])) return pairs[index + 1];
        }
        return normalized;
    }

    private static String code(String value) {
        if (value == null || value.isBlank()) return value;
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutAccents.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
    }
}