package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;

import java.util.List;

public interface ICategoryService {
    List<CategoryResponse> getCategoryTree();

    CategoryResponse getBySlug(String slug);

    CategoryResponse getById(Long id);

    List<CategoryAdminTableResponse> getAdminTable();

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
