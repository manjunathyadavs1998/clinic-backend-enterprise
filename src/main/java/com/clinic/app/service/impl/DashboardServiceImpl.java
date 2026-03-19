package com.clinic.app.service.impl;

import com.clinic.app.dto.dashboard.DashboardStatsResponse;
import com.clinic.app.enums.PaymentStatus;
import com.clinic.app.repository.*;
import com.clinic.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;
    private final BillingRepository billingRepository;
    private final LabTestRepository labTestRepository;

    @Override
    public DashboardStatsResponse getStats() {
        var allBills = billingRepository.findAll();

        Map<String, Long> consultationsByStatus = consultationRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        BigDecimal totalRevenue = allBills.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PAID)
                .map(b -> b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalDoctors(doctorRepository.count())
                .availableDoctors(doctorRepository.findByAvailableTrue().size())
                .totalConsultations(consultationRepository.count())
                .consultationsByStatus(consultationsByStatus)
                .totalBills(allBills.size())
                .pendingBills(allBills.stream().filter(b -> b.getPaymentStatus() == PaymentStatus.PENDING).count())
                .paidBills(allBills.stream().filter(b -> b.getPaymentStatus() == PaymentStatus.PAID).count())
                .totalRevenue(totalRevenue)
                .totalLabTests(labTestRepository.count())
                .activeLabTests(labTestRepository.findAll().stream().filter(l -> Boolean.TRUE.equals(l.getActive())).count())
                .build();
    }
}
