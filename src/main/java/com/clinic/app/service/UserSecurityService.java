package com.clinic.app.service;

import com.clinic.app.entity.User;

public interface UserSecurityService {
    User getCurrentUserEntity(String username);
    boolean isDoctorOwner(Long consultationId, String username);
}
