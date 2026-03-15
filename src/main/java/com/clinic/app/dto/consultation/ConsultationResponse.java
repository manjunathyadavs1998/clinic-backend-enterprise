package com.clinic.app.dto.consultation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ConsultationResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    private String symptoms;
    private Long doctorId;
    private String doctorName;
    private BigDecimal consultationFee;
    private String status;
    private String doctorNotes;
    private LocalDateTime scheduledAt;
    private String createdBy;
}
