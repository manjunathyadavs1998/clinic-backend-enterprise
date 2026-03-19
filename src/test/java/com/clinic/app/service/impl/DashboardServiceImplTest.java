package com.clinic.app.service.impl;

import com.clinic.app.dto.dashboard.DashboardStatsResponse;
import com.clinic.app.entity.*;
import com.clinic.app.enums.ConsultationStatus;
import com.clinic.app.enums.PaymentStatus;
import com.clinic.app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private ConsultationRepository consultationRepository;
    @Mock private BillingRepository billingRepository;
    @Mock private LabTestRepository labTestRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;
    private Doctor doctor;
    private Consultation consultation;
    private Billing paidBilling;
    private Billing pendingBilling;
    private LabTest activeLabTest;
    private LabTest inactiveLabTest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("admin").fullName("Admin").active(true)
                .createdAt(LocalDateTime.now()).build();

        doctor = Doctor.builder().id(1L).user(user).specialization("General")
                .consultationFee(BigDecimal.valueOf(500)).available(true).build();

        consultation = Consultation.builder()
                .id(1L).patientName("John").patientPhone("1234567890").symptoms("Fever")
                .doctor(doctor).consultationFee(BigDecimal.valueOf(500))
                .scheduledAt(LocalDateTime.now()).status(ConsultationStatus.COMPLETED)
                .createdBy(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        paidBilling = Billing.builder()
                .id(1L).consultation(consultation)
                .consultationFee(BigDecimal.valueOf(500)).testTotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(500)).paymentStatus(PaymentStatus.PAID)
                .generatedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        pendingBilling = Billing.builder()
                .id(2L).consultation(consultation)
                .consultationFee(BigDecimal.valueOf(300)).testTotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(300)).paymentStatus(PaymentStatus.PENDING)
                .generatedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        activeLabTest = LabTest.builder().id(1L).name("Blood Test").cost(BigDecimal.valueOf(100)).active(true).build();
        inactiveLabTest = LabTest.builder().id(2L).name("Old Test").cost(BigDecimal.valueOf(50)).active(false).build();
    }

    @Test
    void getStats_shouldReturnCorrectCounts() {
        when(userRepository.count()).thenReturn(5L);
        when(doctorRepository.count()).thenReturn(3L);
        when(doctorRepository.findByAvailableTrue()).thenReturn(List.of(doctor));
        when(consultationRepository.count()).thenReturn(10L);
        when(consultationRepository.findAll()).thenReturn(List.of(consultation));
        when(billingRepository.findAll()).thenReturn(List.of(paidBilling, pendingBilling));
        when(labTestRepository.count()).thenReturn(2L);
        when(labTestRepository.findAll()).thenReturn(List.of(activeLabTest, inactiveLabTest));

        DashboardStatsResponse stats = dashboardService.getStats();

        assertThat(stats.getTotalUsers()).isEqualTo(5L);
        assertThat(stats.getTotalDoctors()).isEqualTo(3L);
        assertThat(stats.getAvailableDoctors()).isEqualTo(1L);
        assertThat(stats.getTotalConsultations()).isEqualTo(10L);
        assertThat(stats.getTotalBills()).isEqualTo(2L);
        assertThat(stats.getPaidBills()).isEqualTo(1L);
        assertThat(stats.getPendingBills()).isEqualTo(1L);
        assertThat(stats.getTotalRevenue()).isEqualByComparingTo("500.00");
        assertThat(stats.getTotalLabTests()).isEqualTo(2L);
        assertThat(stats.getActiveLabTests()).isEqualTo(1L);
    }

    @Test
    void getStats_shouldGroupConsultationsByStatus() {
        Consultation scheduled = Consultation.builder()
                .id(2L).patientName("Jane").patientPhone("0987654321").symptoms("Cold")
                .doctor(doctor).consultationFee(BigDecimal.valueOf(500))
                .scheduledAt(LocalDateTime.now()).status(ConsultationStatus.SCHEDULED)
                .createdBy(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.count()).thenReturn(1L);
        when(doctorRepository.count()).thenReturn(1L);
        when(doctorRepository.findByAvailableTrue()).thenReturn(List.of());
        when(consultationRepository.count()).thenReturn(2L);
        when(consultationRepository.findAll()).thenReturn(List.of(consultation, scheduled));
        when(billingRepository.findAll()).thenReturn(List.of());
        when(labTestRepository.count()).thenReturn(0L);
        when(labTestRepository.findAll()).thenReturn(List.of());

        DashboardStatsResponse stats = dashboardService.getStats();

        assertThat(stats.getConsultationsByStatus()).containsEntry("COMPLETED", 1L);
        assertThat(stats.getConsultationsByStatus()).containsEntry("SCHEDULED", 1L);
    }

    @Test
    void getStats_shouldReturnZeroRevenueWhenNoBills() {
        when(userRepository.count()).thenReturn(0L);
        when(doctorRepository.count()).thenReturn(0L);
        when(doctorRepository.findByAvailableTrue()).thenReturn(List.of());
        when(consultationRepository.count()).thenReturn(0L);
        when(consultationRepository.findAll()).thenReturn(List.of());
        when(billingRepository.findAll()).thenReturn(List.of());
        when(labTestRepository.count()).thenReturn(0L);
        when(labTestRepository.findAll()).thenReturn(List.of());

        DashboardStatsResponse stats = dashboardService.getStats();

        assertThat(stats.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.getTotalBills()).isEqualTo(0L);
    }
}
