package com.clinic.app.dto.consultation;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddConsultationTestsRequest {
    @NotEmpty
    private List<Long> testIds;
}
