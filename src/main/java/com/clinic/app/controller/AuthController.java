package com.clinic.app.controller;

import com.clinic.app.dto.auth.*;
import com.clinic.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @CrossOrigin(origins = "*")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @CrossOrigin(origins = "*")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                       @Valid @RequestBody LogoutRequest request) {
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }
        authService.logout(accessToken, request);
    }
}
