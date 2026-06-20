package com.pixelbase.backend.modules.user.exposed;

import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;

import java.util.Optional;

public interface UserExposedService {
    Optional<UserAuthDto> findAuthInfoByEmail(String email);

    UserAuthDto saveClient(String email, String encodedPassword);

    UserAuthDto saveAdmin(String email, String encodedPassword);

    boolean existsByEmail(String email);
}
