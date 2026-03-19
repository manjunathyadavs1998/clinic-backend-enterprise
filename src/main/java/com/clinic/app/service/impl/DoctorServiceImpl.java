package com.clinic.app.service.impl;

import com.clinic.app.dto.doctor.*;
import com.clinic.app.entity.*;
import com.clinic.app.enums.RoleName;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.*;
import com.clinic.app.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new BadRequestException("Username already exists");
        });

        Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor role not found"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();
        user.getRoles().add(doctorRole);
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .consultationFee(request.getConsultationFee())
                .roomNumber(request.getRoomNumber())
                .available(true)
                .build();

        return map(doctorRepository.save(doctor));
    }

    @Override
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public List<DoctorResponse> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue().stream().map(this::map).toList();
    }

    @Override
    public DoctorResponse getDoctorById(Long doctorId) {
        return map(getEntity(doctorId));
    }

    @Override
    public DoctorResponse updateAvailability(Long doctorId, UpdateDoctorAvailabilityRequest request) {
        Doctor doctor = getEntity(doctorId);
        doctor.setAvailable(request.getAvailable());
        return map(doctorRepository.save(doctor));
    }

    @Override
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = getEntity(doctorId);
        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        userRepository.delete(user);
    }

    private Doctor getEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    private DoctorResponse map(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .username(doctor.getUser().getUsername())
                .specialization(doctor.getSpecialization())
                .consultationFee(doctor.getConsultationFee())
                .available(doctor.getAvailable())
                .roomNumber(doctor.getRoomNumber())
                .build();
    }
}
