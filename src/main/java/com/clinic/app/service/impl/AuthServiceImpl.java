package com.clinic.app.service.impl;

import com.clinic.app.dto.auth.*;
import com.clinic.app.entity.RefreshToken;
import com.clinic.app.entity.User;
import com.clinic.app.exception.BadRequestException;
import com.clinic.app.repository.RefreshTokenRepository;
import com.clinic.app.repository.UserRepository;
import com.clinic.app.security.JwtService;
import com.clinic.app.security.TokenBlacklistService;
import com.clinic.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String accessToken = jwtService.generateAccessToken(new JwtService.UserPrincipalPayload(user.getUsername()));
        String refreshTokenValue = jwtService.generateRefreshToken(new JwtService.UserPrincipalPayload(user.getUsername()));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(new JwtService.UserPrincipalPayload(user.getUsername()));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .roles(user.getRoles().stream().map(role -> role.getName().name()).toList())
                .build();
    }

    @Override
    public void logout(String accessToken, LogoutRequest request) {
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklist(accessToken);
        }

        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
