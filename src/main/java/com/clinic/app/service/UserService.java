package com.clinic.app.service;

import com.clinic.app.dto.user.ChangePasswordRequest;
import com.clinic.app.dto.user.UpdateUserRequest;
import com.clinic.app.dto.user.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getProfile(String username);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void changePassword(Long id, ChangePasswordRequest request);
    UserResponse toggleActive(Long id);
    void deleteUser(Long id);
}
