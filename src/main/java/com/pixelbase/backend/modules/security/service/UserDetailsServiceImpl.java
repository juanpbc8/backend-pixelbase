package com.pixelbase.backend.modules.security.service;

import com.pixelbase.backend.modules.security.domain.UserDetailsImpl;
import com.pixelbase.backend.modules.user.exposed.UserExposedService;
import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserExposedService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthDto userAuthDto = userService.findAuthInfoByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return new UserDetailsImpl(userAuthDto);
    }
}
