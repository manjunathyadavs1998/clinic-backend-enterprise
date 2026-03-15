package com.clinic.app.controller;

import com.clinic.app.dto.consultation.*;
import com.clinic.app.entity.ConsultationTest;
import com.clinic.app.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ConsultationResponse create(@Valid @RequestBody CreateConsultationRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return consultationService.createConsultation(request, userDetails.getUsername());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public List<ConsultationResponse> getAll() {
        return consultationService.getAllConsultations();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ConsultationResponse getById(@PathVariable Long id) {
        return consultationService.getConsultationById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ConsultationResponse updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody UpdateConsultationStatusRequest request,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        return consultationService.updateStatus(id, request, userDetails.getUsername());
    }

    @PatchMapping("/{id}/doctor-notes")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ConsultationResponse updateNotes(@PathVariable Long id,
                                            @Valid @RequestBody UpdateDoctorNotesRequest request,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        return consultationService.updateDoctorNotes(id, request, userDetails.getUsername());
    }

    @PostMapping("/{id}/tests")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public List<ConsultationTest> addTests(@PathVariable Long id,
                                           @Valid @RequestBody AddConsultationTestsRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        return consultationService.addTests(id, request, userDetails.getUsername());
    }

    @GetMapping("/{id}/tests")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public List<ConsultationTest> getTests(@PathVariable Long id) {
        return consultationService.getConsultationTests(id);
    }
}
