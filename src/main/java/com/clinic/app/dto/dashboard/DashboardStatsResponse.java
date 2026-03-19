package com.clinic.app.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {

    // Users
    private long totalUsers;

    // Doctors
    private long totalDoctors;
    private long availableDoctors;

    // Consultations
    private long totalConsultations;
    private Map<String, Long> consultationsByStatus;

    // Billing
    private long totalBills;
    private long pendingBills;
    private long paidBills;
    private BigDecimal totalRevenue;

    // Lab Tests
    private long totalLabTests;
    private long activeLabTests;
}
