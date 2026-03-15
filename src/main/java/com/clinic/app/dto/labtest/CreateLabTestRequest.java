package com.clinic.app.dto.labtest;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLabTestRequest {
    @NotBlank
    private String name;
    @NotNull @DecimalMin("0.0")
    private BigDecimal cost;
}
