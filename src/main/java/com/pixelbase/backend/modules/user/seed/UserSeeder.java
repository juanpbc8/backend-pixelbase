package com.pixelbase.backend.modules.user.seed;

import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import com.pixelbase.backend.modules.user.service.UserInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@Order(4)
@RequiredArgsConstructor
public class UserSeeder implements DataSeeder {
    private final UserInternalService userInternalService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        // 1. Crear 1 Administrador para el panel
        createAdminIfNotExists();

        // 2. Crear 4 clientes de prueba (Mercado Peruano)
        createCustomers();
    }

    private void createAdminIfNotExists() {
        String adminEmail = "admin@pixelbase.pe";
        if (!userInternalService.existsByEmail(adminEmail)) {
            UserEntity admin = UserEntity.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("pixelbase123"))
                .firstName("Administrador")
                .lastName("Pixelbase")
                .phone("999888777")
                .documentType(DocumentType.DNI)
                .documentNumber("00000000")
                .role(Role.ADMIN)
                .build();
            userRepository.save(admin);
            log.info(" ✅ -> UserSeeder: Administrador creado. Email: {}, Pass: {}",
                adminEmail,
                "pixelbase123");
        }
    }

    private void createCustomers() {
        if (userRepository.existsByRole(Role.CLIENTE)) {
            log.info(" ℹ️ -> UserSeeder: Los clientes de prueba ya se encuentran registrados.");
            return;
        }

        List<UserEntity> customers = List.of(
            UserEntity.builder()
                .email("juan.perez@gmail.com")
                .passwordHash(passwordEncoder.encode("cliente123"))
                .firstName("Juan").lastName("Pérez Lucho")
                .phone("945123456").documentType(DocumentType.DNI).documentNumber("74859612")
                .role(Role.CLIENTE).build(),
            UserEntity.builder()
                .email("maria.quispe@outlook.com")
                .passwordHash(passwordEncoder.encode("cliente123"))
                .firstName("María").lastName("Quispe Choque")
                .phone("912345678").documentType(DocumentType.DNI).documentNumber("45612378")
                .role(Role.CLIENTE).build(),
            UserEntity.builder()
                .email("lucho.vidal@yahoo.es")
                .passwordHash(passwordEncoder.encode("cliente123"))
                .firstName("Luis").lastName("Vidal Bazán")
                .phone("955612378").documentType(DocumentType.CE).documentNumber("001234567")
                .role(Role.CLIENTE).build(),
            UserEntity.builder()
                .email("ana.garcia@unmsm.edu.pe")
                .passwordHash(passwordEncoder.encode("cliente123"))
                .firstName("Ana").lastName("García Rosas")
                .phone("944567812").documentType(DocumentType.DNI).documentNumber("87654321")
                .role(Role.CLIENTE).build()
        );

        customers.stream()
            .filter(customer -> !userInternalService.existsByEmail(customer.getEmail()))
            .forEach(userRepository::save);

        log.info(" ✅ -> UserSeeder: {} Clientes peruanos creados. Pass: {}", customers.size(), "cliente123");
    }
}
