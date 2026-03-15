package com.clinic.app.repository;

import com.clinic.app.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
}
