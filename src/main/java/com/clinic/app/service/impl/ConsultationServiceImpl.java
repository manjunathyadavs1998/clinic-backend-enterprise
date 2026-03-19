package com.clinic.app.service.impl;

import com.clinic.app.dto.consultation.*;
import com.clinic.app.entity.*;
import com.clinic.app.enums.*;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.exception.UnauthorizedOperationException;
import com.clinic.app.repository.*;
import com.clinic.app.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final LabTestRepository labTestRepository;
    private final ConsultationTestRepository consultationTestRepository;
    private final BillingRepository billingRepository;

    @Override
    public ConsultationResponse createConsultation(CreateConsultationRequest request, String createdByUsername) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        if (!Boolean.TRUE.equals(doctor.getAvailable())) {
            throw new BadRequestException("Doctor is not available");
        }

        User createdBy = userRepository.findByUsername(createdByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUsername));

        Consultation consultation = Consultation.builder()
                .patientName(request.getPatientName())
                .patientPhone(request.getPatientPhone())
                .symptoms(request.getSymptoms())
                .doctor(doctor)
                .consultationFee(doctor.getConsultationFee())
                .scheduledAt(request.getScheduledAt())
                .status(ConsultationStatus.SCHEDULED)
                .createdBy(createdBy)
                .build();

        return map(consultationRepository.save(consultation));
    }

    @Override
    public List<ConsultationResponse> getAllConsultations() {
        return consultationRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public ConsultationResponse getConsultationById(Long id) {
        return map(getEntity(id));
    }

    @Override
    public ConsultationResponse updateStatus(Long id, UpdateConsultationStatusRequest request, String username) {
        Consultation consultation = getEntity(id);
        validateDoctorOwnershipIfDoctorUser(consultation, username);

        ConsultationStatus current = consultation.getStatus();
        ConsultationStatus next = request.getStatus();

        if (current == ConsultationStatus.COMPLETED && next != ConsultationStatus.COMPLETED) {
            throw new BadRequestException("Completed consultation cannot be changed");
        }

        consultation.setStatus(next);
        return map(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationResponse updateDoctorNotes(Long id, UpdateDoctorNotesRequest request, String username) {
        Consultation consultation = getEntity(id);
        validateDoctorOwnershipIfDoctorUser(consultation, username);
        consultation.setDoctorNotes(request.getDoctorNotes());
        return map(consultationRepository.save(consultation));
    }

    @Override
    public List<ConsultationTest> addTests(Long consultationId, AddConsultationTestsRequest request, String username) {
        Consultation consultation = getEntity(consultationId);
        validateDoctorOwnershipIfDoctorUser(consultation, username);

        List<ConsultationTest> created = new ArrayList<>();
        for (Long testId : request.getTestIds()) {
            LabTest labTest = labTestRepository.findById(testId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lab test not found: " + testId));

            ConsultationTest consultationTest = ConsultationTest.builder()
                    .consultation(consultation)
                    .labTest(labTest)
                    .status(ConsultationTestStatus.ORDERED)
                    .build();

            created.add(consultationTestRepository.save(consultationTest));
        }
        return created;
    }

    @Override
    public List<ConsultationTest> getConsultationTests(Long consultationId) {
        Consultation consultation = getEntity(consultationId);
        return consultationTestRepository.findByConsultation(consultation);
    }

    @Override
    public void deleteConsultation(Long id) {
        Consultation consultation = getEntity(id);
        billingRepository.findByConsultation(consultation).ifPresent(billingRepository::delete);
        consultationTestRepository.deleteAll(consultationTestRepository.findByConsultation(consultation));
        consultationRepository.delete(consultation);
    }

    private Consultation getEntity(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + id));
    }

    private void validateDoctorOwnershipIfDoctorUser(Consultation consultation, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        boolean doctorRole = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_DOCTOR);
        boolean adminRole = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN);
        boolean recepRole = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_RECEPTIONIST);

        if (adminRole || recepRole) {
            return;
        }

        if (doctorRole) {
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + username));

            if (!consultation.getDoctor().getId().equals(doctor.getId())) {
                throw new UnauthorizedOperationException("Doctor can only update own consultations");
            }
        }
    }

    private ConsultationResponse map(Consultation c) {
        return ConsultationResponse.builder()
                .id(c.getId())
                .patientName(c.getPatientName())
                .patientPhone(c.getPatientPhone())
                .symptoms(c.getSymptoms())
                .doctorId(c.getDoctor().getId())
                .doctorName(c.getDoctor().getUser().getFullName())
                .consultationFee(c.getConsultationFee())
                .status(c.getStatus().name())
                .doctorNotes(c.getDoctorNotes())
                .scheduledAt(c.getScheduledAt())
                .createdBy(c.getCreatedBy().getUsername())
                .build();
    }
}
