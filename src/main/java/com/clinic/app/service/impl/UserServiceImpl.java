package com.clinic.app.service.impl;

import com.clinic.app.dto.user.ChangePasswordRequest;
import com.clinic.app.dto.user.UpdateUserRequest;
import com.clinic.app.dto.user.UserResponse;
import com.clinic.app.entity.User;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.DoctorRepository;
import com.clinic.app.repository.UserRepository;
import com.clinic.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return map(getEntity(id));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getEntity(id);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        return map(userRepository.save(user));
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getEntity(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse toggleActive(Long id) {
        User user = getEntity(id);
        user.setActive(!user.getActive());
        return map(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = getEntity(id);
        if (doctorRepository.findByUser(user).isPresent()) {
            throw new BadRequestException("Cannot delete user with a doctor profile. Delete the doctor first.");
        }
        userRepository.delete(user);
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse map(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }
}
