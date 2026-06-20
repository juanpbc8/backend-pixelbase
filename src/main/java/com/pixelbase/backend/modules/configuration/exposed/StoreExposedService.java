package com.pixelbase.backend.modules.configuration.exposed;

import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;

import java.util.Optional;

public interface StoreExposedService {
    Optional<StoreSharedDto> getStoreById(Long id);
}
