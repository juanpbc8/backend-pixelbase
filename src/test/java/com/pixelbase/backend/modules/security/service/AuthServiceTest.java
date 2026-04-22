package com.pixelbase.backend.modules.security.service;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.modules.security.domain.UserDetailsImpl;
import com.pixelbase.backend.modules.security.dto.AuthResponse;
import com.pixelbase.backend.modules.security.dto.LoginRequest;
import com.pixelbase.backend.modules.security.dto.RegisterRequest;
import com.pixelbase.backend.modules.security.jwt.JwtService;
import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private UserEntity existingUser;
    private UserDetailsImpl principal;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        existingUser = UserEntity.builder()
                .id(1)
                .email("test@pixelbase.io")
                .passwordHash("hashed-pwd")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        principal = new UserDetailsImpl(existingUser);
        authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    @DisplayName("login autentica y devuelve AuthResponse con token")
    void loginReturnsAuthResponse() {
        LoginRequest request = new LoginRequest("test@pixelbase.io", "plain-pwd");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token-123");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.email()).isEqualTo("test@pixelbase.io");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("register crea usuario nuevo y hace auto-login")
    void registerCreatesUserAndAutoLogs() {
        RegisterRequest request = new RegisterRequest("nuevo@pixelbase.io", "PasswordSeguro12!");

        when(userService.existsByEmail("nuevo@pixelbase.io")).thenReturn(false);
        when(passwordEncoder.encode("PasswordSeguro12!")).thenReturn("hashed-pwd");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("jwt-new-user");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-new-user");
        verify(userService).register(any(UserEntity.class));
        verify(passwordEncoder).encode("PasswordSeguro12!");
    }

    @Test
    @DisplayName("register falla si el email ya existe")
    void registerFailsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("existente@pixelbase.io", "Password123456!");
        when(userService.existsByEmail("existente@pixelbase.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email ya está registrado");

        verify(userService, never()).register(any());
    }
}
