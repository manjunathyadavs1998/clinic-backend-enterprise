package com.clinic.app.controller;

import com.clinic.app.dto.billing.*;
import com.clinic.app.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/consultations/{consultationId}/generate")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public BillResponse generate(@PathVariable Long consultationId) {
        return billingService.generateBill(consultationId);
    }

    @GetMapping("/consultations/{consultationId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public BillResponse getByConsultationId(@PathVariable Long consultationId) {
        return billingService.getBillByConsultationId(consultationId);
    }

    @PatchMapping("/{billId}/payment")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public BillResponse updatePayment(@PathVariable Long billId,
                                      @Valid @RequestBody UpdatePaymentRequest request) {
        return billingService.updatePayment(billId, request);
    }
}
