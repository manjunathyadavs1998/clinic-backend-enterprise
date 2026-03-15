package com.clinic.app.dto.doctor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDoctorAvailabilityRequest {
    @NotNull
    private Boolean available;
}
