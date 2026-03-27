//package org.bcp.dlmsenrolment.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.bcp.dlmsenrolment.dtos.request.PartnerRequestDto;
//import org.bcp.dlmsenrolment.dtos.request.TransferRequestDto;
//import org.bcp.dlmsenrolment.excptions.ApiResponse;
//import org.bcp.dlmsenrolment.services.PartnerService;
//import org.bcp.dlmsenrolment.services.TransferService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@Slf4j
//public class PartnerController {
//    private final PartnerService partnerService;
//    private final TransferService transferService;
//
//    public PartnerController(PartnerService partnerService, TransferService transferService) {
//        this.partnerService = partnerService;
//        this.transferService = transferService;
//    }
//
//    @GetMapping("getpartner/{partnerId}")
//    public ResponseEntity<ApiResponse<PartnerRequestDto>> getPartner(@PathVariable String partnerId) {
//        return partnerService.getPartner(partnerId);
//    }
//
//    @GetMapping("gealltransfers/{partnerId}")
//    public ResponseEntity<ApiResponse<List<TransferRequestDto>>> getAllTransfer(@PathVariable String partnerId) {
//        return transferService.getAllTransfersByPartnerReferenceNumber(partnerId);
//    }
//
//}
