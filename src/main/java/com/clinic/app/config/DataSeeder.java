package com.clinic.app.config;

import com.clinic.app.entity.*;
import com.clinic.app.enums.RoleName;
import com.clinic.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final LabTestRepository labTestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).description("Admin role").build()));
        Role doctorRole = roleRepository.findByName(RoleName.ROLE_DOCTOR)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_DOCTOR).description("Doctor role").build()));
        Role recepRole = roleRepository.findByName(RoleName.ROLE_RECEPTIONIST)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_RECEPTIONIST).description("Receptionist role").build()));

        User admin = userRepository.findByUsername("admin").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin@123"))
                        .fullName("Manohar MS")
                        .roles(Set.of(adminRole))
                        .build())
        );

        User doctorUser = userRepository.findByUsername("doctor1").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("doctor1")
                        .password(passwordEncoder.encode("doctor@123"))
                        .fullName("Dr Manjunath")
                        .roles(Set.of(doctorRole))
                        .build())
        );

        User doctorUser2 = userRepository.findByUsername("doctor2").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("doctor2")
                        .password(passwordEncoder.encode("doctor@123"))
                        .fullName("Dr Rashmi")
                        .roles(Set.of(doctorRole))
                        .build())
        );

        User doctorUser3 = userRepository.findByUsername("doctor3").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("doctor3")
                        .password(passwordEncoder.encode("doctor@123"))
                        .fullName("Dr David")
                        .roles(Set.of(doctorRole))
                        .build())
        );

        userRepository.findByUsername("staff").orElseGet(() ->
                userRepository.save(User.builder()
                        .username("staff")
                        .password(passwordEncoder.encode("staff@123"))
                        .fullName("Reception One")
                        .roles(Set.of(recepRole))
                        .build())
        );

        doctorRepository.findByUser(doctorUser).orElseGet(() ->
                doctorRepository.save(Doctor.builder()
                        .user(doctorUser)
                        .specialization("General Medicine")
                        .consultationFee(new BigDecimal("300.00"))
                        .available(true)
                        .roomNumber("101")
                        .build())
        );

        doctorRepository.findByUser(doctorUser2).orElseGet(() ->
                doctorRepository.save(Doctor.builder()
                        .user(doctorUser2)
                        .specialization("Gynecologist")
                        .consultationFee(new BigDecimal("300.00"))
                        .available(true)
                        .roomNumber("102")
                        .build())
        );
        doctorRepository.findByUser(doctorUser3).orElseGet(() ->
                doctorRepository.save(Doctor.builder()
                        .user(doctorUser3)
                        .specialization("Neuron Surgeon")
                        .consultationFee(new BigDecimal("300.00"))
                        .available(true)
                        .roomNumber("105")
                        .build())
        );

        if (labTestRepository.count() == 0) {
            labTestRepository.save(LabTest.builder().name("Blood Test").cost(new BigDecimal("500.00")).active(true).build());
            labTestRepository.save(LabTest.builder().name("X-Ray").cost(new BigDecimal("800.00")).active(true).build());
            labTestRepository.save(LabTest.builder().name("ECG").cost(new BigDecimal("600.00")).active(true).build());
        }
    }
}
