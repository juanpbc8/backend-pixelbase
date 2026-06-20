package com.pixelbase.backend.modules.user.service;

public interface UserInternalService {
    boolean existsByEmail(String email);
}
