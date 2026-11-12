package org.bcp.dlmsenrolment.services.impl;

import org.bcp.dlmsenrolment.utils.PartnerValueNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.PartnerCurrencyRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.PartnerCurrencyResponseDto;
import org.bcp.dlmsenrolment.entities.Partner;
import org.bcp.dlmsenrolment.entities.PartnerCurrency;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.mappers.PartnerCurrencyMapper;
import org.bcp.dlmsenrolment.repositories.PartnerCurrencyRepository;
import org.bcp.dlmsenrolment.repositories.PartnerRepository;
import org.bcp.dlmsenrolment.services.PartnerCurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PartnerCurrencyServiceImpl implements PartnerCurrencyService {
    private final PartnerCurrencyRepository repository;
    private final PartnerRepository partnerRepository;
    private final PartnerCurrencyMapper mapper;
    public PartnerCurrencyServiceImpl(PartnerCurrencyRepository repository, PartnerRepository partnerRepository, PartnerCurrencyMapper mapper) {
        this.repository = repository;
        this.partnerRepository = partnerRepository;
        this.mapper = mapper;
    }


    @Override
    public ResponseEntity<ApiResponse<PartnerCurrencyRequestDto>> getPartnerCurrencies(String referenceNumber) {
        // 1. Find the partner. If not found, this will correctly throw an exception.
        Partner partner = partnerRepository
                .findByPartnerReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("No partner with reference number: " + referenceNumber));

        // 2. Fetch the COMPLETE LIST of currency entities for this partner.
        List<PartnerCurrency> currencyEntities = repository.findAllByPartner(partner);

        // 3. Extract just the currency codes (the strings) from the list of entities.
        // Using a stream is a clean and modern way to do this transformation.
        Set<String> currencyCodes = currencyEntities.stream()
                .map(PartnerCurrency::getCurrency) // Method reference to get the currency string
                .collect(Collectors.toSet());

        // 4. Create ONE DTO to hold the final, aggregated result.
        PartnerCurrencyRequestDto response = new PartnerCurrencyRequestDto();
        response.setPartnerReferenceNumber(partner.getPartnerReferenceNumber());
        response.setCurrencies(currencyCodes);

        // 5. Return the single, correctly populated DTO.
        return ResponseEntity.ok(ApiResponse.success("Partner currencies retrieved successfully.", response));
    }


    @Override
    public ResponseEntity<ApiResponse<PartnerCurrencyResponseDto>> createPartnerCurrency(PartnerCurrencyRequestDto requestDto) {
        // 1. Verify the partner exists. (No changes needed)
        Partner partner = partnerRepository
                .findByPartnerReferenceNumber(requestDto.getPartnerReferenceNumber())
                .orElseThrow(() -> new RuntimeException("No partner with reference number: "
                        + requestDto.getPartnerReferenceNumber()));
        if (!PartnerValueNormalizer.isActive(partner.getPartnerStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("PARTNER_INACTIVE", "Cannot add currencies to an inactive partner", "partnerStatus"));
        }

        // 2. Fetch the set of currency codes that the partner ALREADY has. (No changes needed)
        Set<String> existingCurrencyCodes = repository.findCurrencyCodesByPartner(partner);

        // 3. Filter the incoming list to find only the currencies that are genuinely new. (No changes needed)
        Set<String> newCurrenciesToAdd = requestDto.getCurrencies().stream()
                .filter(incomingCurrency -> !existingCurrencyCodes.contains(incomingCurrency))
                .collect(Collectors.toSet());

        // 4. If there are no new currencies to add, return a DTO with an empty list.
        // This is more informative than returning an empty array `[]`.
        if (newCurrenciesToAdd.isEmpty()) {
            PartnerCurrencyResponseDto responseDto = new PartnerCurrencyResponseDto();
            responseDto.setPartnerReferenceNumber(partner.getPartnerReferenceNumber());
            responseDto.setCurrencies(Set.of());
            return ResponseEntity.ok(ApiResponse.success("No new currencies to add.", responseDto));
        }

        // 5. Create entity objects ONLY for the new currencies. (No changes needed)
        List<PartnerCurrency> partnerCurrencies = newCurrenciesToAdd.stream()
                .map(currencyCode -> {
                    PartnerCurrency partnerCurrency = new PartnerCurrency();
                    partnerCurrency.setPartner(partner);
                    partnerCurrency.setCurrency(currencyCode);
                    return partnerCurrency;
                })
                .collect(Collectors.toList());

        // 6. Save all the new currency entities in a single, efficient operation. (No changes needed)
        List<PartnerCurrency> savedCurrencies = repository.saveAll(partnerCurrencies);

        // ==========================================================
        //  START: REVISED LOGIC FOR CREATING THE RESPONSE
        // ==========================================================

        // 7. Extract just the currency codes (the strings) from the list of entities that were just saved.
        Set<String> savedCurrencyCodes = savedCurrencies.stream()
                .map(PartnerCurrency::getCurrency)
                .collect(Collectors.toSet());

        // 8. Create ONE DTO to hold the aggregated result.
        PartnerCurrencyResponseDto response = new PartnerCurrencyResponseDto();
        response.setPartnerReferenceNumber(partner.getPartnerReferenceNumber());
        response.setCurrencies(savedCurrencyCodes); // Set the list of newly added currencies

        // 9. Return the single, correctly populated DTO.
        return ResponseEntity.ok(ApiResponse.success("Partner currency created successfully.", response));
    }




//    @Override
//    public ResponseEntity<PartnerCurrencyDto> createPartnerCurrency(PartnerCurrencyDto partnerCurrencyDto) {
//        // Verify if partner exists
//        Partner partner = partnerRepository
//                .findByPartnerReferenceNumber(partnerCurrencyDto.getPartnerReferenceNumber())
//                .orElseThrow(() -> new RuntimeException("No partner with reference number: "
//                        + partnerCurrencyDto.getPartnerReferenceNumber()));
//
//        // Check if currency already exists for this partner
//        PartnerCurrency existingCurrencyOpt = repository.findByPartner(partner);
//        if (existingCurrencyOpt != null) {
//            throw new RuntimeException("Currency already exists for this partner");
//        }
//
//        // Create new PartnerCurrency
//        List<PartnerCurrency> partnerCurrencies = new ArrayList<>();
//        for (String currencyCode : partnerCurrencyDto.getCurrencies()) {
//            PartnerCurrency partnerCurrency = new PartnerCurrency();
//            partnerCurrency.setPartner(partner);
//            partnerCurrency.setCurrency(currencyCode);
//            partnerCurrencies.add(partnerCurrency);
//        }
//
//
//
//        // Save and return
//        List<PartnerCurrency> savedCurrency = repository.saveAll(partnerCurrencies);;
//        return ResponseEntity.ok(mapper.toResponseDto(savedCurrency));
//    }





    @Override
    public ResponseEntity<ApiResponse<PartnerCurrencyResponseDto>> updatePartnerCurrencies(PartnerCurrencyRequestDto partnerCurrencyRequestDto) {
        // 1. Verify partner exists. (No change)
        Partner partner = partnerRepository
                .findByPartnerReferenceNumber(partnerCurrencyRequestDto.getPartnerReferenceNumber())
                .orElseThrow(() -> new RuntimeException("No partner with reference number: "
                        + partnerCurrencyRequestDto.getPartnerReferenceNumber()));
        if (!PartnerValueNormalizer.isActive(partner.getPartnerStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("PARTNER_INACTIVE", "Cannot update currencies for an inactive partner", "partnerStatus"));
        }

        // 2. Fetch the current list of currencies.
        Set<String> existingCurrencies = repository.findCurrencyCodesByPartner(partner);

        // =================================================================================
        //  START: NEW GUARD CLAUSE
        // =================================================================================
        if (existingCurrencies.isEmpty()) {
            throw new IllegalStateException("Cannot update currencies for partner '"
                    + partnerCurrencyRequestDto.getPartnerReferenceNumber()
                    + "' because they have no currencies assigned. Use the create endpoint first.");
        }
        // =================================================================================
        //  END: NEW GUARD CLAUSE
        // =================================================================================


        // 3. Calculate the delta (what to add, what to remove). This logic remains the same.
        Set<String> newCurrencies = partnerCurrencyRequestDto.getCurrencies();

        List<PartnerCurrency> currenciesToAdd = newCurrencies.stream()
                .filter(currency -> !existingCurrencies.contains(currency))
                .map(currency -> {
                    PartnerCurrency newPartnerCurrency = new PartnerCurrency();
                    newPartnerCurrency.setPartner(partner);
                    newPartnerCurrency.setCurrency(currency);
                    return newPartnerCurrency;
                })
                .collect(Collectors.toList());

        Set<String> currenciesToRemove = existingCurrencies.stream()
                .filter(currency -> !newCurrencies.contains(currency))
                .collect(Collectors.toSet());

        // 4. Perform database operations only if there are changes. (No change)
        if (!currenciesToAdd.isEmpty()) {
            repository.saveAll(currenciesToAdd);
        }
        if (!currenciesToRemove.isEmpty()) {
            repository.deleteAllByPartnerAndCurrencyIn(partner, currenciesToRemove);
        }

        // 5. Build and return the final state. (No change)
        List<PartnerCurrency> finalCurrencyEntities = repository.findAllByPartner(partner);
        Set<String> finalCurrencyCodes = finalCurrencyEntities.stream()
                .map(PartnerCurrency::getCurrency)
                .collect(Collectors.toSet());

        PartnerCurrencyResponseDto responseDto = new PartnerCurrencyResponseDto();
        responseDto.setPartnerReferenceNumber(partner.getPartnerReferenceNumber());
        responseDto.setCurrencies(finalCurrencyCodes);

        return ResponseEntity.ok(ApiResponse.success("Partner currencies updated successfully.", responseDto));
    }

//
//    @Override
//    public ResponseEntity<PartnerCurrencyDto> updatePartnerCurrencies(PartnerCurrencyDto partnerCurrencyDto) {
//        // Verify partner exists
//        Partner partner = partnerRepository
//                .findByPartnerReferenceNumber(partnerCurrencyDto.getPartnerReferenceNumber())
//                .orElseThrow(() -> new RuntimeException("No partner with reference number: "
//                        + partnerCurrencyDto.getPartnerReferenceNumber()));
//
//        // Find Currency in table PartnerCurrency by partner
//        PartnerCurrency existingCurrency = repository.findByPartner(partner);
//        if (existingCurrency == null) {
//            throw new RuntimeException("Currency does not exist for this partner");
//        }
//
//        // Update currency value
//        existingCurrency.setCurrency(partnerCurrencyDto.getCurrency());
//
//        // Save updated currency
//        PartnerCurrency updatedCurrency = repository.save(existingCurrency);
//
//        // Return updated DTO
//        return ResponseEntity.ok(mapper.toResponseDto(updatedCurrency));
//    }


}
