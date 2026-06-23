package com.pixelbase.backend.modules.user.repository;

import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    // Válida unicidad de documento: ¿Existe un usuario con este DNI cuyo ID sea DIFERENTE al mío?
    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);
}
