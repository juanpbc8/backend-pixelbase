package com.pixelbase.backend.modules.user.service;

import com.pixelbase.backend.modules.user.domain.UserEntity;

public interface IUserService {
    UserEntity findByEmail(String email);

    UserEntity register(UserEntity user);

    boolean existsByEmail(String email);
}
