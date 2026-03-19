package com.clinic.app.controller;

import com.clinic.app.dto.labtest.*;
import com.clinic.app.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public List<LabTestResponse> getAll() {
        return labTestService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public LabTestResponse create(@Valid @RequestBody CreateLabTestRequest request) {
        return labTestService.create(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LabTestResponse update(@PathVariable Long id, @Valid @RequestBody UpdateLabTestRequest request) {
        return labTestService.update(id, request);
    }
}
