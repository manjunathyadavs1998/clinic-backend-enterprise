package com.clinic.app.service.impl;

import com.clinic.app.dto.billing.BillResponse;
import com.clinic.app.dto.billing.UpdatePaymentRequest;
import com.clinic.app.entity.*;
import com.clinic.app.enums.ConsultationStatus;
import com.clinic.app.enums.PaymentMode;
import com.clinic.app.enums.PaymentStatus;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.BillingRepository;
import com.clinic.app.repository.ConsultationRepository;
import com.clinic.app.repository.ConsultationTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock private BillingRepository billingRepository;
    @Mock private ConsultationRepository consultationRepository;
    @Mock private ConsultationTestRepository consultationTestRepository;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Consultation consultation;
    private Billing billing;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("staff").fullName("Staff").active(true)
                .createdAt(LocalDateTime.now()).build();
        Doctor doctor = Doctor.builder().id(1L).user(user).specialization("General")
                .consultationFee(new BigDecimal("500.00")).available(true).build();

        consultation = Consultation.builder()
                .id(1L).patientName("John").patientPhone("1234567890").symptoms("Fever")
                .doctor(doctor).consultationFee(new BigDecimal("500.00"))
                .scheduledAt(LocalDateTime.now()).status(ConsultationStatus.COMPLETED)
                .createdBy(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        billing = Billing.builder()
                .id(1L).consultation(consultation)
                .consultationFee(new BigDecimal("500.00"))
                .testTotal(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("500.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .generatedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateBill_shouldCreateNewBillWhenNoneExists() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationTestRepository.findByConsultation(consultation)).thenReturn(List.of());
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.empty());
        when(billingRepository.save(any())).thenReturn(billing);

        BillResponse response = billingService.generateBill(1L);

        assertThat(response.getConsultationId()).isEqualTo(1L);
        assertThat(response.getConsultationFee()).isEqualByComparingTo("500.00");
        verify(billingRepository).save(any());
    }

    @Test
    void generateBill_shouldThrowWhenConsultationNotFound() {
        when(consultationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.generateBill(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateBill_shouldUpdateExistingBill() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationTestRepository.findByConsultation(consultation)).thenReturn(List.of());
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.of(billing));
        when(billingRepository.save(any())).thenReturn(billing);

        billingService.generateBill(1L);

        verify(billingRepository).save(billing);
    }

    @Test
    void getAllBills_shouldReturnList() {
        when(billingRepository.findAll()).thenReturn(List.of(billing));

        List<BillResponse> result = billingService.getAllBills();

        assertThat(result).hasSize(1);
    }

    @Test
    void getBillById_shouldReturnResponse() {
        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

        BillResponse response = billingService.getBillById(1L);

        assertThat(response.getBillId()).isEqualTo(1L);
        assertThat(response.getPaymentStatus()).isEqualTo("PENDING");
    }

    @Test
    void getBillById_shouldThrowWhenNotFound() {
        when(billingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getBillById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBillByConsultationId_shouldReturnResponse() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.of(billing));

        BillResponse response = billingService.getBillByConsultationId(1L);

        assertThat(response.getBillId()).isEqualTo(1L);
    }

    @Test
    void getBillByConsultationId_shouldThrowWhenBillNotFound() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getBillByConsultationId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePayment_shouldUpdateStatusAndMode() {
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        request.setPaymentStatus(PaymentStatus.PAID);
        request.setPaymentMode(PaymentMode.CASH);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
        when(billingRepository.save(any())).thenReturn(billing);

        BillResponse response = billingService.updatePayment(1L, request);

        assertThat(billing.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(billing.getPaymentMode()).isEqualTo(PaymentMode.CASH);
    }

    @Test
    void updatePayment_shouldThrowWhenPaidButNoMode() {
        UpdatePaymentRequest request = new UpdatePaymentRequest();
        request.setPaymentStatus(PaymentStatus.PAID);
        request.setPaymentMode(null);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

        assertThatThrownBy(() -> billingService.updatePayment(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment mode");
    }

    @Test
    void deleteBill_shouldDelete() {
        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

        billingService.deleteBill(1L);

        verify(billingRepository).delete(billing);
    }

    @Test
    void deleteBill_shouldThrowWhenNotFound() {
        when(billingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.deleteBill(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
