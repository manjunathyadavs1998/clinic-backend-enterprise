package com.clinic.app.repository;

import com.clinic.app.entity.Consultation;
import com.clinic.app.entity.ConsultationTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationTestRepository extends JpaRepository<ConsultationTest, Long> {
    List<ConsultationTest> findByConsultation(Consultation consultation);
}
