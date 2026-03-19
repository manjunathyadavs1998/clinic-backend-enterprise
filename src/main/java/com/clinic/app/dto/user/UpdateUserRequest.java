package com.clinic.app.dto.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String email;
}
