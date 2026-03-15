package com.clinic.app.dto.billing;

import com.clinic.app.enums.PaymentMode;
import com.clinic.app.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentRequest {
    @NotNull
    private PaymentStatus paymentStatus;
    private PaymentMode paymentMode;
}
