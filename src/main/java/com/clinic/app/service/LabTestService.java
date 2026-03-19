package com.clinic.app.service;

import com.clinic.app.dto.labtest.*;

import java.util.List;

public interface LabTestService {
    LabTestResponse create(CreateLabTestRequest request);
    List<LabTestResponse> getAll();
    LabTestResponse getById(Long id);
    LabTestResponse update(Long id, UpdateLabTestRequest request);
    void delete(Long id);
}
