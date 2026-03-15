package com.clinic.app.dto.consultation;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateConsultationRequest {
    @NotBlank
    private String patientName;
    @NotBlank
    private String patientPhone;
    @NotBlank
    private String symptoms;
    @NotNull
    private Long doctorId;
    @NotNull
    private LocalDateTime scheduledAt;
}
