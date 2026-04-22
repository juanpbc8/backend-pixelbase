package com.pixelbase.backend.integration;

import com.pixelbase.backend.modules.security.dto.AuthResponse;
import com.pixelbase.backend.modules.security.dto.LoginRequest;
import com.pixelbase.backend.modules.security.dto.RegisterRequest;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Integration test: levanta el contexto completo con H2 en memoria
 * y valida el flujo register → login → uso del JWT.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.cors.allowed-origins=http://localhost:4200",
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "jwt.expiration=86400000"
})
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("register crea usuario y devuelve JWT")
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("integration@pixelbase.io", "PasswordSeguro12!");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().email()).isEqualTo("integration@pixelbase.io");
        assertThat(response.getBody().role()).isEqualTo("CUSTOMER");
        assertThat(userRepository.existsByEmail("integration@pixelbase.io")).isTrue();
    }

    @Test
    @DisplayName("login con credenciales válidas devuelve token")
    void loginWithValidCredentialsReturnsToken() {
        // Primero registrar
        restTemplate.postForEntity("/api/v1/auth/register",
                new RegisterRequest("user@pixelbase.io", "PasswordSeguro12!"),
                AuthResponse.class);

        // Luego login
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                new LoginRequest("user@pixelbase.io", "PasswordSeguro12!"),
                AuthResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
    }

    @Test
    @DisplayName("endpoint protegido sin token devuelve 401")
    void protectedEndpointWithoutTokenReturns401() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/products", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("endpoint protegido con JWT válido devuelve 200")
    void protectedEndpointWithValidTokenReturns200() {
        // Registrar y obtener token
        ResponseEntity<AuthResponse> auth = restTemplate.postForEntity(
                "/api/v1/auth/register",
                new RegisterRequest("flow@pixelbase.io", "PasswordSeguro12!"),
                AuthResponse.class);
        String token = auth.getBody().token();

        // Llamar endpoint protegido con el token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
