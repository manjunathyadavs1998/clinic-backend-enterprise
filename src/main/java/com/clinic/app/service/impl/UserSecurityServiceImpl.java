package com.clinic.app.service.impl;

import com.clinic.app.entity.Consultation;
import com.clinic.app.entity.Doctor;
import com.clinic.app.entity.User;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.ConsultationRepository;
import com.clinic.app.repository.DoctorRepository;
import com.clinic.app.repository.UserRepository;
import com.clinic.app.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;

    @Override
    public User getCurrentUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Override
    public boolean isDoctorOwner(Long consultationId, String username) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + consultationId));
        User user = getCurrentUserEntity(username);
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + username));
        return consultation.getDoctor().getId().equals(doctor.getId());
    }
}
