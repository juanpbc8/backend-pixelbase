package com.pixelbase.backend.modules.user.service.impl;

import com.pixelbase.backend.modules.user.repository.UserRepository;
import com.pixelbase.backend.modules.user.service.UserInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInternalServiceImpl implements UserInternalService {
    private final UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
