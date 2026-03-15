package com.clinic.app.service;

import com.clinic.app.dto.auth.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String accessToken, LogoutRequest request);
}
