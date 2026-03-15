package com.clinic.app.dto.consultation;

import com.clinic.app.enums.ConsultationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConsultationStatusRequest {
    @NotNull
    private ConsultationStatus status;
}
