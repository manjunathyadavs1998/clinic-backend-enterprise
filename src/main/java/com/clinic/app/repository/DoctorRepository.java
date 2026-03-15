package com.clinic.app.repository;

import com.clinic.app.entity.Doctor;
import com.clinic.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    List<Doctor> findByAvailableTrue();
}
