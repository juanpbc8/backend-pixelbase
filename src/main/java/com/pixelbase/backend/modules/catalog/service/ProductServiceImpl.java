package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtils;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductImageEntity;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductResponse;
import com.pixelbase.backend.modules.catalog.mapper.ProductMapper;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import com.pixelbase.backend.modules.catalog.repository.ProductRepository;
import com.pixelbase.backend.modules.catalog.repository.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public PageResponse<ProductCardResponse> getStorefrontProducts(
            String search, Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        // 1. Iniciamos la especificación base: Siempre filtrar por productos activos
        // para el storefront público.
        Specification<ProductEntity> specs = Specification.unrestricted();

        // 2. Agregamos filtros dinámicos solo si vienen en el request.
        specs = specs.and(ProductSpecification.isActive())
                .and(ProductSpecification.hasSearch(search))
                .and(ProductSpecification.hasCategory(categoryId))
                .and(ProductSpecification.hasBrand(brandId))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice));

        // 3. Ejecutamos la consulta paginada en el repositorio.
        Page<ProductEntity> productPage = productRepository.findAll(specs, pageable);

        // 4. Mapeamos la página de entidades a una página de DTO (Cards).
        List<ProductCardResponse> content = productPage.getContent().stream()
                .map(productMapper::toCardResponse)
                .toList();

        // 5. Envolvemos en nuestro PageResponse para el frontend.
        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    public ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", slug));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        // 1. Validar unicidad de SKU
        if (productRepository.existsBySku(request.sku())) {
            throw new ConflictException("El SKU ya existe");
        }

        // 2. Mapear request a entidad (sin relaciones aún)
        ProductEntity product = productMapper.toEntity(request);
        product.setSlug(SlugUtils.toSlug(request.name())); // Slug automático

        // 3. Resolver Marca y Categoría por ID
        product.setBrand(brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca", request.brandId())));
        product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", request.categoryId())));

        // 4. Sincronizar imágenes usando el method helper "addImage"
        if (request.images() != null) {
            request.images().forEach(imgDto -> {
                ProductImageEntity image = ProductImageEntity.builder()
                        .url(imgDto.url())
                        .altText(imgDto.altText())
                        .position(imgDto.position())
                        .build();
                product.addImage(image); // Mantiene sincronizada la relación bidireccional
            });
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));

        // Actualización de campos básicos
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setSpecifications(request.specifications()); // JSONB flexible

        // Solo regeneramos el slug si el nombre cambió (Opcional, según negocio)
        product.setSlug(SlugUtils.toSlug(request.name()));

        // Resolver nuevas relaciones si cambiaron
        product.setBrand(brandRepository.findById(request.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca", request.brandId())));
        product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", request.categoryId())));

        return productMapper.toResponse(product); // El Dirty Checking hará el UPDATE
    }

    @Override
    public PageResponse<ProductResponse> getAdminProducts(
            String search, Long categoryId, Long brandId, Pageable pageable) {

        // 1. Para el Admin NO usamos isActive(), permitiendo ver completamente el catálogo.
        Specification<ProductEntity> specs = Specification.unrestricted();
        specs = specs.and(ProductSpecification.hasSearch(search))
                .and(ProductSpecification.hasCategory(categoryId))
                .and(ProductSpecification.hasBrand(brandId));

        Page<ProductEntity> productPage = productRepository.findAll(specs, pageable);

        // 2. Devolvemos ProductResponse (Completo) para que el admin gestione stock y estados.
        return new PageResponse<>(
                productPage.getContent().stream().map(productMapper::toResponse).toList(),
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Override
    @Transactional
    public void updateStatus(Long id, ProductStatusRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        product.setStatus(request.status());
        // No necesitamos llamar a save() explícitamente gracias a @Transactional
        // y al estado 'Managed' de JPA.
    }
}