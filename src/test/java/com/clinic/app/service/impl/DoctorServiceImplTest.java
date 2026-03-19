package com.clinic.app.service.impl;

import com.clinic.app.dto.doctor.CreateDoctorRequest;
import com.clinic.app.dto.doctor.DoctorResponse;
import com.clinic.app.dto.doctor.UpdateDoctorAvailabilityRequest;
import com.clinic.app.entity.Doctor;
import com.clinic.app.entity.Role;
import com.clinic.app.entity.User;
import com.clinic.app.enums.RoleName;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.DoctorRepository;
import com.clinic.app.repository.RoleRepository;
import com.clinic.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private User user;
    private Doctor doctor;
    private Role doctorRole;

    @BeforeEach
    void setUp() {
        doctorRole = new Role();
        doctorRole.setId(1L);
        doctorRole.setName(RoleName.ROLE_DOCTOR);

        user = User.builder()
                .id(1L).username("doctor1").password("encoded").fullName("Dr. Smith")
                .phone("1234567890").email("dr@clinic.com").active(true)
                .createdAt(LocalDateTime.now()).build();

        doctor = Doctor.builder()
                .id(1L).user(user).specialization("General Medicine")
                .consultationFee(new BigDecimal("500.00")).available(true)
                .roomNumber("101").createdAt(LocalDateTime.now()).build();
    }

    @Test
    void createDoctor_shouldSaveAndReturnResponse() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUsername("doctor1");
        request.setPassword("password");
        request.setFullName("Dr. Smith");
        request.setSpecialization("General Medicine");
        request.setConsultationFee(new BigDecimal("500.00"));
        request.setRoomNumber("101");

        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_DOCTOR)).thenReturn(Optional.of(doctorRole));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(doctorRepository.save(any())).thenReturn(doctor);

        DoctorResponse response = doctorService.createDoctor(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("Dr. Smith");
        assertThat(response.getSpecialization()).isEqualTo("General Medicine");
        assertThat(response.getAvailable()).isTrue();
    }

    @Test
    void createDoctor_shouldThrowWhenUsernameExists() {
        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setUsername("doctor1");

        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> doctorService.createDoctor(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getAllDoctors_shouldReturnList() {
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));

        List<DoctorResponse> result = doctorService.getAllDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("doctor1");
    }

    @Test
    void getAvailableDoctors_shouldReturnOnlyAvailable() {
        when(doctorRepository.findByAvailableTrue()).thenReturn(List.of(doctor));

        List<DoctorResponse> result = doctorService.getAvailableDoctors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailable()).isTrue();
    }

    @Test
    void getDoctorById_shouldReturnResponse() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getDoctorById_shouldThrowWhenNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAvailability_shouldUpdateFlag() {
        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest();
        request.setAvailable(false);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any())).thenReturn(doctor);

        DoctorResponse response = doctorService.updateAvailability(1L, request);

        assertThat(doctor.getAvailable()).isFalse();
    }

    @Test
    void deleteDoctor_shouldDeleteDoctorAndUser() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.deleteDoctor(1L);

        verify(doctorRepository).delete(doctor);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteDoctor_shouldThrowWhenNotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(doctorRepository, never()).delete(any(Doctor.class));
    }
}
