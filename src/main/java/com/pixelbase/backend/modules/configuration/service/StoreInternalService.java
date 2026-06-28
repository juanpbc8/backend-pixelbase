package com.pixelbase.backend.modules.configuration.service;

import com.pixelbase.backend.modules.configuration.api.web.dto.response.StoreResponse;

import java.util.List;

public interface StoreInternalService {
    List<StoreResponse> getActiveStoresForStorefront();
}
