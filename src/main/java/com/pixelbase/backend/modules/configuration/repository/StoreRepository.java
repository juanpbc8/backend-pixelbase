package com.pixelbase.backend.modules.configuration.repository;

import com.pixelbase.backend.modules.configuration.domain.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    List<StoreEntity> findByActiveTrue();
}
