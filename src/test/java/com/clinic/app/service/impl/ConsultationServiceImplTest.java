package com.clinic.app.service.impl;

import com.clinic.app.dto.consultation.*;
import com.clinic.app.entity.*;
import com.clinic.app.enums.ConsultationStatus;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceImplTest {

    @Mock private ConsultationRepository consultationRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private UserRepository userRepository;
    @Mock private LabTestRepository labTestRepository;
    @Mock private ConsultationTestRepository consultationTestRepository;
    @Mock private BillingRepository billingRepository;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private User user;
    private Doctor doctor;
    private Consultation consultation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("staff").password("encoded").fullName("Staff User")
                .active(true).createdAt(LocalDateTime.now()).build();

        doctor = Doctor.builder()
                .id(1L).user(user).specialization("General Medicine")
                .consultationFee(new BigDecimal("500.00")).available(true)
                .roomNumber("101").createdAt(LocalDateTime.now()).build();

        consultation = Consultation.builder()
                .id(1L).patientName("John Doe").patientPhone("1234567890").symptoms("Fever")
                .doctor(doctor).consultationFee(new BigDecimal("500.00"))
                .scheduledAt(LocalDateTime.now()).status(ConsultationStatus.SCHEDULED)
                .createdBy(user).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createConsultation_shouldCreateAndReturnResponse() {
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setDoctorId(1L);
        request.setPatientName("John Doe");
        request.setPatientPhone("1234567890");
        request.setSymptoms("Fever");
        request.setScheduledAt(LocalDateTime.now());

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(user));
        when(consultationRepository.save(any())).thenReturn(consultation);

        ConsultationResponse response = consultationService.createConsultation(request, "staff");

        assertThat(response.getPatientName()).isEqualTo("John Doe");
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        assertThat(response.getDoctorId()).isEqualTo(1L);
    }

    @Test
    void createConsultation_shouldThrowWhenDoctorNotAvailable() {
        doctor.setAvailable(false);
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setDoctorId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> consultationService.createConsultation(request, "staff"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createConsultation_shouldThrowWhenDoctorNotFound() {
        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setDoctorId(99L);

        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.createConsultation(request, "staff"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllConsultations_shouldReturnList() {
        when(consultationRepository.findAll()).thenReturn(List.of(consultation));

        List<ConsultationResponse> result = consultationService.getAllConsultations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientName()).isEqualTo("John Doe");
    }

    @Test
    void getConsultationById_shouldReturnResponse() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        ConsultationResponse response = consultationService.getConsultationById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getConsultationById_shouldThrowWhenNotFound() {
        when(consultationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.getConsultationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_shouldChangeStatus() {
        UpdateConsultationStatusRequest request = new UpdateConsultationStatusRequest();
        request.setStatus(ConsultationStatus.IN_PROGRESS);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(user));
        when(consultationRepository.save(any())).thenReturn(consultation);

        consultationService.updateStatus(1L, request, "staff");

        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
    }

    @Test
    void updateStatus_shouldThrowWhenTryingToChangeCompleted() {
        consultation.setStatus(ConsultationStatus.COMPLETED);
        UpdateConsultationStatusRequest request = new UpdateConsultationStatusRequest();
        request.setStatus(ConsultationStatus.SCHEDULED);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> consultationService.updateStatus(1L, request, "staff"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Completed consultation");
    }

    @Test
    void deleteConsultation_shouldDeleteBillingTestsAndConsultation() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.empty());
        when(consultationTestRepository.findByConsultation(consultation)).thenReturn(List.of());

        consultationService.deleteConsultation(1L);

        verify(consultationRepository).delete(consultation);
        verify(consultationTestRepository).deleteAll(any());
    }

    @Test
    void deleteConsultation_shouldAlsoDeleteBillingIfExists() {
        Billing billing = Billing.builder().id(1L).consultation(consultation)
                .consultationFee(BigDecimal.valueOf(500)).testTotal(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(500))
                .generatedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(billingRepository.findByConsultation(consultation)).thenReturn(Optional.of(billing));
        when(consultationTestRepository.findByConsultation(consultation)).thenReturn(List.of());

        consultationService.deleteConsultation(1L);

        verify(billingRepository).delete(billing);
        verify(consultationRepository).delete(consultation);
    }
}
