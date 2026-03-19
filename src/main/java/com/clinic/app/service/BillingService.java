package com.clinic.app.service;

import com.clinic.app.dto.billing.BillResponse;
import com.clinic.app.dto.billing.UpdatePaymentRequest;

import java.util.List;

public interface BillingService {
    BillResponse generateBill(Long consultationId);
    List<BillResponse> getAllBills();
    BillResponse getBillByConsultationId(Long consultationId);
    BillResponse getBillById(Long billId);
    BillResponse updatePayment(Long billId, UpdatePaymentRequest request);
    void deleteBill(Long billId);
}
