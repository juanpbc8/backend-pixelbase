package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;

import java.util.List;

public interface IBrandService {
    List<BrandResponse> getAll();

    BrandResponse getById(Long id);

    BrandResponse create(BrandRequest request);
}