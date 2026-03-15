package com.clinic.app.dto.doctor;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String username;
    private String specialization;
    private BigDecimal consultationFee;
    private Boolean available;
    private String roomNumber;
}
