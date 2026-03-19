package com.clinic.app.controller;

import com.clinic.app.dto.user.ChangePasswordRequest;
import com.clinic.app.dto.user.UpdateUserRequest;
import com.clinic.app.dto.user.UserResponse;
import com.clinic.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public void changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse toggleActive(@PathVariable Long id) {
        return userService.toggleActive(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
