package com.clinic.app.service;

import com.clinic.app.dto.billing.BillResponse;
import com.clinic.app.dto.billing.UpdatePaymentRequest;

public interface BillingService {
    BillResponse generateBill(Long consultationId);
    BillResponse getBillByConsultationId(Long consultationId);
    BillResponse getBillById(Long billId);
    BillResponse updatePayment(Long billId, UpdatePaymentRequest request);
    void deleteBill(Long billId);
}
