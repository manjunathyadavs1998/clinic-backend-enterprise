package com.clinic.app.dto.billing;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillResponse {
    private Long billId;
    private Long consultationId;
    private BigDecimal consultationFee;
    private BigDecimal testTotal;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMode;
}
