package com.clinic.app.dto.doctor;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDoctorRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String fullName;
    @NotBlank
    private String specialization;
    @NotNull @DecimalMin("0.0")
    private BigDecimal consultationFee;
    private String roomNumber;
    private String phone;
    private String email;
}
