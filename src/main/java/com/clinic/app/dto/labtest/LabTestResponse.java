package com.clinic.app.dto.labtest;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LabTestResponse {
    private Long id;
    private String name;
    private BigDecimal cost;
    private Boolean active;
}
