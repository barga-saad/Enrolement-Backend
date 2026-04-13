package org.bcp.dlmsenrolment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/partners")
    @Operation(summary = "Export partners as CSV")
    public ResponseEntity<byte[]> exportPartnersCsv() {
        log.info("Exporting partners to CSV");
        List<Partner> partners = partnerRepository.findAll();

        StringWriter sw = new StringWriter();

        // CSV Header
        sw.append("ID,Name,Reference Number,Type,Status,Country,Classification,Account Number,")
          .append("Main Currency,Payment Nature,")
          .append("Internal Instant,External Instant,Standard,Cash Pickup,Mass Cash Pickup,Wallet,")
          .append("Limit Internal,Limit External,Limit Standard,Limit Cash Pickup,Limit Mass Cash,Limit Wallet\n");

        // CSV Rows
        for (Partner p : partners) {
            sw.append(escapeCsv(str(p.getId())))
              .append(',').append(escapeCsv(p.getName()))
              .append(',').append(escapeCsv(p.getPartnerReferenceNumber()))
              .append(',').append(escapeCsv(p.getPartnerType()))
              .append(',').append(escapeCsv(p.getPartnerStatus()))
              .append(',').append(escapeCsv(p.getHeadquarterCountry()))
              .append(',').append(escapeCsv(p.getClassification()))
              .append(',').append(escapeCsv(p.getAccountNumber()))
              .append(',').append(escapeCsv(p.getMainCurrency()))
              .append(',').append(escapeCsv(p.getPaymentNature()))
              .append(',').append(str(p.isAllowedInternalInstantTransfer()))
              .append(',').append(str(p.isAllowedExternalInstantTransfer()))
              .append(',').append(str(p.isAllowedStandardTransfer()))
              .append(',').append(str(p.isAllowedCashPickupTransfer()))
              .append(',').append(str(p.isAllowedMassCashPickupTransfer()))
              .append(',').append(str(p.isAllowedWalletTransfer()))
              .append(',').append(str(p.getLimitInternalInstantTransfer()))
              .append(',').append(str(p.getLimitExternalInstantTransfer()))
              .append(',').append(str(p.getLimitStandardTransfer()))
              .append(',').append(str(p.getLimitCashPickupTransfer()))
              .append(',').append(str(p.getLimitMassCashPickupTransfer()))
              .append(',').append(str(p.getLimitWalletTransfer()))
              .append('\n');
        }

        byte[] csvBytes = sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=partners.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
