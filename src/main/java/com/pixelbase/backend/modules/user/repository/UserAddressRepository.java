package com.pixelbase.backend.modules.user.repository;

import com.pixelbase.backend.modules.user.domain.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddressEntity, Long> {
    // Lista solo las direcciones del cliente autenticado
    List<UserAddressEntity> findAllByUserId(Long userId);

    // Blindaje anti-IDOR: Busca una dirección asegurando que le pertenezca al usuario
    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    // Method optimizado para apagar todas las direcciones predeterminadas del usuario en una sola consulta
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserAddressEntity a SET a.defaulted = false WHERE a.user.id = :userId")
    void resetDefaultAddressesByUserId(@Param("userId") Long userId);
}
