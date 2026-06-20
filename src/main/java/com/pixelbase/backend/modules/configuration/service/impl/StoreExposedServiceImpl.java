package com.pixelbase.backend.modules.configuration.service.impl;

import com.pixelbase.backend.modules.configuration.exposed.StoreExposedService;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import com.pixelbase.backend.modules.configuration.mapper.StoreMapper;
import com.pixelbase.backend.modules.configuration.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreExposedServiceImpl implements StoreExposedService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    public Optional<StoreSharedDto> getStoreById(Long id) {
        return storeRepository.findById(id)
            .map(storeMapper::toSharedDto);
    }
}
