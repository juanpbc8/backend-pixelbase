package com.pixelbase.backend.modules.configuration.service.impl;

import com.pixelbase.backend.modules.configuration.domain.StoreEntity;
import com.pixelbase.backend.modules.configuration.dto.response.StoreResponse;
import com.pixelbase.backend.modules.configuration.mapper.StoreMapper;
import com.pixelbase.backend.modules.configuration.repository.StoreRepository;
import com.pixelbase.backend.modules.configuration.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreInternalServiceImpl implements StoreInternalService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    public List<StoreResponse> getActiveStoresForStorefront() {
        List<StoreEntity> activeStores = storeRepository.findByActiveTrue();
        return storeMapper.toResponseList(activeStores);
    }
}
