package com.pixelbase.backend.modules.user.service.impl;

import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.exposed.UserExposedService;
import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;
import com.pixelbase.backend.modules.user.mapper.UserMapper;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExposedServiceImpl implements UserExposedService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<UserAuthDto> findAuthInfoByEmail(String email) {
        return userRepository.findByEmail(email)
            .map(userMapper::toAuthDto);
    }

    @Override
    @Transactional
    public UserAuthDto saveClient(String email, String encodedPassword) {
        return saveUserWithRole(email, encodedPassword, Role.CLIENTE.name());
    }

    @Override
    @Transactional
    public UserAuthDto saveAdmin(String email, String encodedPassword) {
        return saveUserWithRole(email, encodedPassword, Role.ADMIN.name());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserAuthDto saveUserWithRole(String email, String encodedPassword, String role) {
        UserEntity newUser = UserEntity.builder()
            .email(email)
            .passwordHash(encodedPassword)
            .role(Role.valueOf(role))
            .enabled(true)
            .build();

        UserEntity savedUser = userRepository.save(newUser);
        return userMapper.toAuthDto(savedUser);
    }
}
