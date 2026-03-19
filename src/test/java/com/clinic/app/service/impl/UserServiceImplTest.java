package com.clinic.app.service.impl;

import com.clinic.app.dto.user.ChangePasswordRequest;
import com.clinic.app.dto.user.UpdateUserRequest;
import com.clinic.app.dto.user.UserResponse;
import com.clinic.app.entity.Doctor;
import com.clinic.app.entity.User;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.exception.ResourceNotFoundException;
import com.clinic.app.repository.DoctorRepository;
import com.clinic.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .fullName("Test User")
                .phone("1234567890")
                .email("test@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getProfile_shouldReturnUserResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("testuser");

        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void getProfile_shouldThrowWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_shouldReturnResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_shouldUpdateProvidedFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Updated Name");
        request.setPhone("9876543210");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(user.getFullName()).isEqualTo("Updated Name");
        assertThat(user.getPhone()).isEqualTo("9876543210");
        assertThat(user.getEmail()).isEqualTo("test@example.com"); // unchanged
    }

    @Test
    void updateUser_shouldNotOverwriteNullFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        // all null — nothing should change

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateUser(1L, request);

        assertThat(user.getFullName()).isEqualTo("Test User");
        assertThat(user.getPhone()).isEqualTo("1234567890");
    }

    @Test
    void changePassword_shouldEncodeAndSave() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_encoded");
        when(userRepository.save(any())).thenReturn(user);

        userService.changePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("new_encoded");
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void toggleActive_shouldFlipActiveFlag() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.toggleActive(1L);

        assertThat(user.getActive()).isFalse();

        userService.toggleActive(1L);

        assertThat(user.getActive()).isTrue();
    }

    @Test
    void deleteUser_shouldDeleteWhenNoDoctorProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.findByUser(user)).thenReturn(Optional.empty());

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowWhenDoctorProfileExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.findByUser(user)).thenReturn(Optional.of(mock(Doctor.class)));

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("doctor profile");
        verify(userRepository, never()).delete(any());
    }
}
