package com.clinic.app.controller;

import com.clinic.app.dto.doctor.*;
import com.clinic.app.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public List<DoctorResponse> getAll() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/available")
    @CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<DoctorResponse> getAvailable() {
        return doctorService.getAvailableDoctors();
    }

    @GetMapping("/{id}")
    @CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public DoctorResponse getById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @PostMapping
    @CrossOrigin(origins = "*")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse create(@Valid @RequestBody CreateDoctorRequest request) {
        return doctorService.createDoctor(request);
    }

    @PatchMapping("/{id}/availability")
    @CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public DoctorResponse updateAvailability(@PathVariable Long id,
                                             @Valid @RequestBody UpdateDoctorAvailabilityRequest request) {
        return doctorService.updateAvailability(id, request);
    }
}
