package com.clinic.app.repository;

import com.clinic.app.entity.Billing;
import com.clinic.app.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    Optional<Billing> findByConsultation(Consultation consultation);
}
