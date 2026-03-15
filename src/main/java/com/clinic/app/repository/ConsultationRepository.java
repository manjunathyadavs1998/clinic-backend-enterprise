package com.clinic.app.repository;

import com.clinic.app.entity.Consultation;
import com.clinic.app.entity.Doctor;
import com.clinic.app.enums.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByDoctor(Doctor doctor);
    List<Consultation> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);
    List<Consultation> findByStatus(ConsultationStatus status);
}
