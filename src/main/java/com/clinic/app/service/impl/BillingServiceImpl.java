package com.clinic.app.service.impl;

import com.clinic.app.dto.billing.*;
import com.clinic.app.entity.*;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.*;
import com.clinic.app.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationTestRepository consultationTestRepository;

    @Override
    public List<BillResponse> getAllBills() {
        return billingRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public BillResponse generateBill(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + consultationId));

        List<ConsultationTest> tests = consultationTestRepository.findByConsultation(consultation);
        BigDecimal testTotal = tests.stream()
                .map(ct -> ct.getLabTest().getCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Billing billing = billingRepository.findByConsultation(consultation)
                .orElseGet(() -> Billing.builder().consultation(consultation).build());

        billing.setConsultationFee(consultation.getConsultationFee());
        billing.setTestTotal(testTotal);
        billing.setTotalAmount(consultation.getConsultationFee().add(testTotal));

        return map(billingRepository.save(billing));
    }

    @Override
    public BillResponse getBillByConsultationId(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + consultationId));
        Billing billing = billingRepository.findByConsultation(consultation)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for consultation: " + consultationId));
        return map(billing);
    }

    @Override
    public BillResponse getBillById(Long billId) {
        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));
        return map(billing);
    }

    @Override
    public BillResponse updatePayment(Long billId, UpdatePaymentRequest request) {
        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

        if (request.getPaymentStatus().name().equals("PAID") && request.getPaymentMode() == null) {
            throw new BadRequestException("Payment mode is required when status is PAID");
        }

        billing.setPaymentStatus(request.getPaymentStatus());
        billing.setPaymentMode(request.getPaymentMode());
        return map(billingRepository.save(billing));
    }

    @Override
    public void deleteBill(Long billId) {
        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));
        billingRepository.delete(billing);
    }

    private BillResponse map(Billing billing) {
        return BillResponse.builder()
                .billId(billing.getId())
                .consultationId(billing.getConsultation().getId())
                .consultationFee(billing.getConsultationFee())
                .testTotal(billing.getTestTotal())
                .totalAmount(billing.getTotalAmount())
                .paymentStatus(billing.getPaymentStatus().name())
                .paymentMode(billing.getPaymentMode() == null ? null : billing.getPaymentMode().name())
                .build();
    }
}
