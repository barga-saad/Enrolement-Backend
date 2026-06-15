package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.security.SecurityUtils;
import org.bcp.dlmsenrolment.services.AuditService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringWriter;
import java.util.List;

@RestController
@RequestMapping("/v1/export")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Export", description = "Data export endpoints")
public class ExportController {

    private final PartnerRepository partnerRepository;
    private final AuditService auditService;

    @GetMapping("/partners")
    @Operation(summary = "Export validated partners as CSV — all fields")
    public ResponseEntity<byte[]> exportPartnersCsv(Authentication authentication) {
        String actor = SecurityUtils.getUsername(authentication);
        log.info("User {} exporting VALIDATED partners to CSV", actor);

        List<Partner> partners = partnerRepository.findByWorkflowStatus("VALIDATED");

        StringWriter sw = new StringWriter();

        // ── HEADER ─────────────────────────────────────────────────────────────
        sw.append("ID,Nom,Référence,Type Partenaire,Statut Partenaire,Pays Siège,Pays Société Mère,Classification,N° Compte,Canal,")
          .append("Enrollment Type,Workflow Status,Créé par,Créé le,Mis à jour le,")
          // Transferts autorisés
          .append("Virt Instantané Intra BP,Virt Instantané Confrère,Virt Standard,Cash Pickup,Mass Cash Pickup,Wallet,")
          // Plafonds
          .append("Plafond Intra BP,Plafond Instantané Confrère,Plafond Standard,Plafond Cash Pickup,Plafond Mass Cash,Plafond Wallet,")
          .append("Nature Mass Cash Pickup,Validité Mass Cash (jours),")
          // Devise & change
          .append("Devise Principale,Nature Taux Change,Gain Change %,")
          // Facturation abonnement
          .append("Frais Abonnement,Périodicité Abonnement,Compte Facturation Abonnement,")
          // Conditions tarifaires
          .append("Nature Paiement,Mode Frais,Facturation Mensuelle,Débit Immédiat,Reversement,")
          // Commissions devise
          .append("Devise Commission,")
          // Commission Intra BP
          .append("Type Comm Intra BP,Montant Comm Intra BP,Taux Comm Intra BP,Min Comm Intra BP,Max Comm Intra BP,")
          // Commission Instantané Confrère
          .append("Type Comm Instantané Confrère,Montant Comm Inst Confrère,Taux Comm Inst Confrère,Min Comm Inst Confrère,Max Comm Inst Confrère,")
          // Commission Standard
          .append("Type Comm Standard,Montant Comm Standard,Taux Comm Standard,Min Comm Standard,Max Comm Standard,")
          // Commission Mass Cash Pickup
          .append("Type Comm Mass Cash,Montant Comm Mass Cash,Taux Comm Mass Cash,Min Comm Mass Cash,Max Comm Mass Cash,")
          // Commission Cash Pickup
          .append("Type Comm Cash Pickup,Montant Comm Cash Pickup,Taux Comm Cash Pickup,Min Comm Cash Pickup,Max Comm Cash Pickup,")
          // Commission Wallet
          .append("Type Comm Wallet,Montant Comm Wallet,Taux Comm Wallet,Min Comm Wallet,Max Comm Wallet,")
          // Forex Réception
          .append("Autoriser Réception,Devises Réception,Plafond Réception,")
          // Forex Émission
          .append("Autoriser Émission,Type Transaction Émission,Autorisation OC,Réf Autorisation OC,Devises Émission,Nature Cours Change Émission,Plafond Montant Devise,")
          // Conformité DO
          .append("Conf DO Type Pièce,Conf DO N° Pièce,Conf DO Date Naissance,Conf DO Pays Naissance,Conf DO Adresse,Conf DO Ville Résidence,")
          // Conformité Bénéficiaire
          .append("Conf Bénéf Type Pièce,Conf Bénéf N° Pièce,Conf Bénéf Adresse,Conf Bénéf Ville Résidence,Conf Bénéf Pays Résidence,")
          // Fichier synthèse & Facturation
          .append("Fichier Synthèse,Setup Fee,Setup Fee Facturé,Mode Facturation,Dérogation,Détails Dérogation\n");

        // ── ROWS ───────────────────────────────────────────────────────────────
        for (Partner p : partners) {
            sw.append(str(p.getId())).append(',')
              .append(csv(p.getName())).append(',')
              .append(csv(p.getPartnerReferenceNumber())).append(',')
              .append(csv(p.getPartnerType())).append(',')
              .append(csv(p.getPartnerStatus())).append(',')
              .append(csv(p.getHeadquarterCountry())).append(',')
              .append(csv(p.getParentCompanyCountry())).append(',')
              .append(csv(p.getClassification())).append(',')
              .append(csv(p.getAccountNumber())).append(',')
              .append(csv(p.getCanal())).append(',')
              .append(csv(p.getEnrollmentType())).append(',')
              .append(csv(p.getWorkflowStatus())).append(',')
              .append(csv(p.getCreatedBy())).append(',')
              .append(str(p.getCreatedAt())).append(',')
              .append(str(p.getUpdatedAt())).append(',')
              // Transferts autorisés
              .append(bool(p.isAllowedInternalInstantTransfer())).append(',')
              .append(bool(p.isAllowedExternalInstantTransfer())).append(',')
              .append(bool(p.isAllowedStandardTransfer())).append(',')
              .append(bool(p.isAllowedCashPickupTransfer())).append(',')
              .append(bool(p.isAllowedMassCashPickupTransfer())).append(',')
              .append(bool(p.isAllowedWalletTransfer())).append(',')
              // Plafonds
              .append(str(p.getLimitInternalInstantTransfer())).append(',')
              .append(str(p.getLimitExternalInstantTransfer())).append(',')
              .append(str(p.getLimitStandardTransfer())).append(',')
              .append(str(p.getLimitCashPickupTransfer())).append(',')
              .append(str(p.getLimitMassCashPickupTransfer())).append(',')
              .append(str(p.getLimitWalletTransfer())).append(',')
              .append(str(p.getNatureMassCashPickupTransfer())).append(',')
              .append(str(p.getValidDaysMassCashPickupTransfer())).append(',')
              // Devise & change
              .append(csv(p.getMainCurrency())).append(',')
              .append(csv(p.getExchangeRateNature())).append(',')
              .append(str(p.getExchangeGainPercentage())).append(',')
              // Abonnement
              .append(str(p.getSubscriptionFee())).append(',')
              .append(csv(p.getSubscriptionPeriodicity())).append(',')
              .append(str(p.getSubscriptionBillingAccount())).append(',')
              // Conditions tarifaires
              .append(csv(p.getPaymentNature())).append(',')
              .append(csv(p.getFeeMode())).append(',')
              .append(bool(p.getMonthlyBilling())).append(',')
              .append(bool(p.getImmediateDebit())).append(',')
              .append(bool(p.getReimbursement())).append(',')
              // Commissions
              .append(csv(p.getCommissionCurrency())).append(',')
              .append(csv(p.getCommissionTypeInstantInternal())).append(',')
              .append(str(p.getCommissionAmountInstantInternal())).append(',')
              .append(str(p.getCommissionRateInstantInternal())).append(',')
              .append(str(p.getCommissionMinInstantInternal())).append(',')
              .append(str(p.getCommissionMaxInstantInternal())).append(',')
              .append(csv(p.getCommissionTypeInstantExternal())).append(',')
              .append(str(p.getCommissionAmountInstantExternal())).append(',')
              .append(str(p.getCommissionRateInstantExternal())).append(',')
              .append(str(p.getCommissionMinInstantExternal())).append(',')
              .append(str(p.getCommissionMaxInstantExternal())).append(',')
              .append(csv(p.getCommissionTypeStandardExternal())).append(',')
              .append(str(p.getCommissionAmountStandardExternal())).append(',')
              .append(str(p.getCommissionRateStandardExternal())).append(',')
              .append(str(p.getCommissionMinStandardExternal())).append(',')
              .append(str(p.getCommissionMaxStandardExternal())).append(',')
              .append(csv(p.getCommissionTypeMassCashPickup())).append(',')
              .append(str(p.getCommissionAmountMassCashPickup())).append(',')
              .append(str(p.getCommissionRateMassCashPickup())).append(',')
              .append(str(p.getCommissionMinMassCashPickup())).append(',')
              .append(str(p.getCommissionMaxMassCashPickup())).append(',')
              .append(csv(p.getCommissionTypeCashPickup())).append(',')
              .append(str(p.getCommissionAmountCashPickup())).append(',')
              .append(str(p.getCommissionRateCashPickup())).append(',')
              .append(str(p.getCommissionMinCashPickup())).append(',')
              .append(str(p.getCommissionMaxCashPickup())).append(',')
              .append(csv(p.getCommissionTypeWallet())).append(',')
              .append(str(p.getCommissionAmountWallet())).append(',')
              .append(str(p.getCommissionRateWallet())).append(',')
              .append(str(p.getCommissionMinWallet())).append(',')
              .append(str(p.getCommissionMaxWallet())).append(',')
              // Forex Réception
              .append(bool(p.getAutoriserReception())).append(',')
              .append(csv(p.getDevisesReception())).append(',')
              .append(str(p.getPlafondReception())).append(',')
              // Forex Émission
              .append(bool(p.getAutoriserEmission())).append(',')
              .append(csv(p.getTypeTransactionEmission())).append(',')
              .append(bool(p.getAutorisationOC())).append(',')
              .append(csv(p.getRefAutorisationOC())).append(',')
              .append(csv(p.getDevisesEmission())).append(',')
              .append(csv(p.getNatureCoursChangeEmission())).append(',')
              .append(str(p.getPlafondMontantDevise())).append(',')
              // Conformité DO
              .append(bool(p.getConformiteDOTypePiece())).append(',')
              .append(bool(p.getConformiteDONumPiece())).append(',')
              .append(bool(p.getConformiteDODateNaissance())).append(',')
              .append(bool(p.getConformiteDOPaysNaissance())).append(',')
              .append(bool(p.getConformiteDOAdresse())).append(',')
              .append(bool(p.getConformiteDOVilleResidence())).append(',')
              // Conformité Bénéficiaire
              .append(bool(p.getConformiteBenefTypePiece())).append(',')
              .append(bool(p.getConformiteBenefNumPiece())).append(',')
              .append(bool(p.getConformiteBenefAdresse())).append(',')
              .append(bool(p.getConformiteBenefVilleResidence())).append(',')
              .append(bool(p.getConformiteBenefPaysResidence())).append(',')
              // Fichier synthèse & Facturation
              .append(bool(p.getFichierSynthese())).append(',')
              .append(str(p.getSetupFee())).append(',')
              .append(bool(p.getSetupFeeBilled())).append(',')
              .append(csv(p.getBillingMode())).append(',')
              .append(bool(p.getDerogation())).append(',')
              .append(csv(p.getDerogationDetails()))
              .append('\n');
        }

        byte[] csvBytes = sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        auditService.log(actor, "EXPORT_PARTNERS_CSV", "PARTNER", null,
                "User " + actor + " exported " + partners.size() + " validated partners to CSV");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=partners_validated_" + java.time.LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    /** Escape a field for RFC-4180 CSV */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String bool(Boolean value) {
        if (value == null) return "";
        return value ? "Oui" : "Non";
    }

    private String bool(boolean value) {
        return value ? "Oui" : "Non";
    }
}