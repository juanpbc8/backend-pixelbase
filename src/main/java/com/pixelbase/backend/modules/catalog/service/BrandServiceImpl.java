package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtils;
import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.dto.response.BrandResponse;
import com.pixelbase.backend.modules.catalog.mapper.BrandMapper;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements IBrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Override
    public BrandResponse getById(Long id) {
        return brandMapper.toResponse(brandRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca", id)));
    }

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        BrandEntity entity = brandMapper.toEntity(request);
        entity.setSlug(SlugUtils.toSlug(request.name()));

        return brandMapper.toResponse(brandRepository.save(entity));
    }
}
