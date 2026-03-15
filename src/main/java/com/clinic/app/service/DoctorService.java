package com.clinic.app.service;

import com.clinic.app.dto.doctor.*;

import java.util.List;

public interface DoctorService {
    DoctorResponse createDoctor(CreateDoctorRequest request);
    List<DoctorResponse> getAllDoctors();
    List<DoctorResponse> getAvailableDoctors();
    DoctorResponse getDoctorById(Long doctorId);
    DoctorResponse updateAvailability(Long doctorId, UpdateDoctorAvailabilityRequest request);
}
