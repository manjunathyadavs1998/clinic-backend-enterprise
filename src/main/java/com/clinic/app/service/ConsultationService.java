package com.clinic.app.service;

import com.clinic.app.dto.consultation.*;
import com.clinic.app.entity.ConsultationTest;

import java.util.List;

public interface ConsultationService {
    ConsultationResponse createConsultation(CreateConsultationRequest request, String createdByUsername);
    List<ConsultationResponse> getAllConsultations();
    ConsultationResponse getConsultationById(Long id);
    ConsultationResponse updateStatus(Long id, UpdateConsultationStatusRequest request, String username);
    ConsultationResponse updateDoctorNotes(Long id, UpdateDoctorNotesRequest request, String username);
    List<ConsultationTest> addTests(Long consultationId, AddConsultationTestsRequest request, String username);
    List<ConsultationTest> getConsultationTests(Long consultationId);
    void deleteConsultation(Long id);
}
