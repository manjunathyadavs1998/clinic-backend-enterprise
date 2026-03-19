package com.clinic.app.dto.labtest;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLabTestRequest {
    private String name;
    @DecimalMin("0.0")
    private BigDecimal cost;
    private Boolean active;
}
