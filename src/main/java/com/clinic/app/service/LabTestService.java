package com.clinic.app.service;

import com.clinic.app.dto.labtest.*;

import java.util.List;

public interface LabTestService {
    LabTestResponse create(CreateLabTestRequest request);
    List<LabTestResponse> getAll();
}
