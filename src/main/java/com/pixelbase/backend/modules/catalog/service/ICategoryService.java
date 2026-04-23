package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getCategoryTree(); // Para el Navbar

    CategoryResponse getById(Long id);

    CategoryResponse create(CategoryRequest request);
}