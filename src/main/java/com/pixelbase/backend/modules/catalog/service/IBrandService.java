package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;

import java.util.List;

public interface IBrandService {
    List<BrandResponse> getAll();

    List<BrandAdminTableResponse> getAdminTable();

    BrandResponse getById(Long id);

    BrandResponse getBySlug(String slug);

    BrandResponse create(BrandRequest request);

    BrandResponse update(Long id, BrandRequest request);

    void delete(Long id);
}
