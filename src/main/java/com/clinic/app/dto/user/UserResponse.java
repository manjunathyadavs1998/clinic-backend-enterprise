package com.clinic.app.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
