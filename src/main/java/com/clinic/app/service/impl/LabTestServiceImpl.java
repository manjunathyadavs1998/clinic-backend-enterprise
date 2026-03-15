package com.clinic.app.service.impl;

import com.clinic.app.dto.labtest.*;
import com.clinic.app.entity.LabTest;
import com.clinic.app.repository.LabTestRepository;
import com.clinic.app.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;

    @Override
    public LabTestResponse create(CreateLabTestRequest request) {
        LabTest labTest = LabTest.builder()
                .name(request.getName())
                .cost(request.getCost())
                .active(true)
                .build();
        return map(labTestRepository.save(labTest));
    }

    @Override
    public List<LabTestResponse> getAll() {
        return labTestRepository.findAll().stream().map(this::map).toList();
    }

    private LabTestResponse map(LabTest labTest) {
        return LabTestResponse.builder()
                .id(labTest.getId())
                .name(labTest.getName())
                .cost(labTest.getCost())
                .active(labTest.getActive())
                .build();
    }
}
