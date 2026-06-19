package com.pixelbase.backend.modules.catalog.service.impl;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtil;
import com.pixelbase.backend.modules.catalog.domain.BrandEntity;
import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import com.pixelbase.backend.modules.catalog.dto.request.ProductRequest;
import com.pixelbase.backend.modules.catalog.dto.request.ProductStatusRequest;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminDetailResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductCardResponse;
import com.pixelbase.backend.modules.catalog.dto.response.ProductDetailResponse;
import com.pixelbase.backend.modules.catalog.mapper.ProductMapper;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import com.pixelbase.backend.modules.catalog.repository.ProductRepository;
import com.pixelbase.backend.modules.catalog.repository.specification.ProductSpecification;
import com.pixelbase.backend.modules.catalog.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
        Specification<ProductEntity> specs = ProductSpecification.isActive();

        // 2. Agregamos filtros dinámicos solo si vienen en el request.
        specs = specs
            .and(ProductSpecification.hasSearch(search))
            .and(ProductSpecification.hasCategoryHierarchical(categoryId))
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
    public ProductDetailResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
            .map(productMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe un producto registrado con el slug '%s'.",
                slug)));
    }

    @Override
    @Transactional
    public ProductAdminDetailResponse create(ProductRequest request) {
        String normalizedName = request.name().trim().toUpperCase();
        String normalizedPartNumber = request.partNumber().trim().toUpperCase();

        // 1. Validaciones de negocio locales en RAM (Evitan llamadas innecesarias a BD)
        validatePricePromotion(request.price(), request.originalPrice());
        validateStockStatus(request.stock(), request.status());

        // 2. Existencia de relaciones (Evita consultas innecesarias si fallan)
        BrandEntity brand = findBrandByIdOrThrow(request.brandId());
        CategoryEntity category = findCategoryByIdOrThrow(request.categoryId());

        // 3. Cálculos semánticos y de tokens de sistema
        String generatedSlug = SlugUtil.toSlug(normalizedName);

        // 4. Validación de unicidad cruzada en Base de Datos (Entidad limpia)
        validateProductUniqueness(normalizedName, normalizedPartNumber, generatedSlug, null);

        // 5. Consumo de secuencias de Postgres e identificadores de negocio
        Long nextSequence = productRepository.getNextSkuSequence();
        String generatedSku = String.format("%06d", nextSequence);

        // Reempaquetado inmutable con datos limpios para el mapper
        ProductRequest normalizedRequest = new ProductRequest(
            normalizedName,
            request.description(),
            request.price(),
            request.originalPrice(),
            request.stock(),
            normalizedPartNumber,
            request.status(),
            request.brandId(),
            request.categoryId(),
            request.specifications(),
            request.images()
        );

        // 6. Mapeo y construcción inicial de la estructura POJO
        ProductEntity product = productMapper.toEntity(normalizedRequest);

        // 7. Sobre escritura de campos controlados y punteros relacionales validados
        product.setSku(generatedSku);
        product.setSlug(generatedSlug);
        product.setName(normalizedName);
        product.setPartNumber(normalizedPartNumber);
        product.setOriginalPrice(normalizedRequest.originalPrice());
        product.setBrand(brand);
        product.setCategory(category);

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.toAdminDetailResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductAdminDetailResponse update(Long id, ProductRequest request) {
        // 1. Recuperar estado actual del recurso desde el búfer de Hibernate
        ProductEntity product = findProductByIdOrThrow(id);
        String currentName = product.getName();

        String normalizedName = request.name().trim().toUpperCase();
        String normalizedPartNumber = request.partNumber().trim().toUpperCase();

        // 2. Validaciones de negocio en caliente sobre el contrato de entrada
        validatePricePromotion(request.price(), request.originalPrice());
        validateStockStatus(request.stock(), request.status());

        // 3. Existencia de relaciones (Evita consultas innecesarias si fallan)
        BrandEntity brand = findBrandByIdOrThrow(request.brandId());
        CategoryEntity category = findCategoryByIdOrThrow(request.categoryId());

        // 4. Cálculo de Slug semántico (Solo regeneramos el slug si el nombre cambió)
        String effectiveSlug = product.getSlug();
        if (!Objects.equals(normalizedName, currentName)) {
            effectiveSlug = SlugUtil.toSlug(normalizedName);
        }

        // 5. Validación de unicidad cruzada en Base de Datos (Entidad limpia)
        validateProductUniqueness(normalizedName, normalizedPartNumber, effectiveSlug, product.getId());

        // Reempaquetado inmutable con datos limpios para el mapper
        ProductRequest normalizedRequest = new ProductRequest(
            normalizedName,
            request.description(),
            request.price(),
            request.originalPrice(),
            request.stock(),
            normalizedPartNumber,
            request.status(),
            request.brandId(),
            request.categoryId(),
            request.specifications(),
            request.images()
        );

        // 6. Mutación segura vía MapStruct (Hibernate registrará el estado sucio de forma limpia)
        productMapper.updateEntityFromRequest(normalizedRequest, product);

        // 7. Sobreescritura de campos controlados y punteros relacionales validados
        product.setSlug(effectiveSlug);
        product.setName(normalizedName);
        product.setPartNumber(normalizedPartNumber);
        product.setOriginalPrice(normalizedRequest.originalPrice());
        product.setBrand(brand);
        product.setCategory(category);

        ProductEntity savedProduct = productRepository.save(product);
        return productMapper.toAdminDetailResponse(savedProduct);
    }

    @Override
    public PageResponse<ProductAdminTableResponse> getAdminProducts(
        String search, Long categoryId, Long brandId, Pageable pageable) {

        // 1. Para el Admin NO usamos isActive(), permitiendo ver completamente el catálogo.
        Specification<ProductEntity> specs = Specification.unrestricted();
        specs = specs.and(ProductSpecification.hasSearch(search))
            .and(ProductSpecification.hasCategoryHierarchical(categoryId))
            .and(ProductSpecification.hasBrand(brandId));

        Page<ProductEntity> productPage = productRepository.findAll(specs, pageable);

        // 2. Devolvemos ProductDetailResponse (Completo) para que el admin gestione stock y estados.
        return new PageResponse<>(
            productPage.map(productMapper::toAdminTableResponse).getContent(),
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.isLast()
        );
    }

    @Override
    public ProductAdminDetailResponse getAdminById(Long id) {
        return productMapper.toAdminDetailResponse(findProductByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void updateStatus(Long id, ProductStatusRequest request) {
        ProductEntity product = findProductByIdOrThrow(id);
        validateStockStatus(product.getStock(), request.status());
        product.setStatus(request.status());
        // No necesitamos llamar a save() explícitamente gracias a @Transactional
        // y al estado 'Managed' de JPA.
    }

    /**
     * Busca una marca por identificador y falla si no existe.
     *
     * @param id identificador de la marca
     * @throws ResourceNotFoundException cuando la marca no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al consultar la marca
     */
    private BrandEntity findBrandByIdOrThrow(Long id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una marca registrada con ID %d.",
                id
            )));
    }

    /**
     * Busca una categoría por identificador y falla si no existe.
     *
     * @param id identificador de la categoría
     * @throws ResourceNotFoundException cuando la categoría no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al consultar la categoría
     */
    private CategoryEntity findCategoryByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con ID %d.",
                id
            )));
    }

    /**
     * Busca un producto por identificador y falla si no existe.
     *
     * @param id identificador del producto
     * @throws ResourceNotFoundException cuando el producto no existe
     * @throws RuntimeException          cuando ocurre un error inesperado al consultar el producto
     */
    private ProductEntity findProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe un producto registrado con ID %d.",
                id
            )));
    }

    /**
     * Válida la unicidad de nombre, slug y número de parte del producto.
     *
     * @param name       nombre comercial del producto
     * @param partNumber número de parte normalizado
     * @param slug       slug generado desde el nombre
     * @param currentId  identificador actual para evitar falsos positivos
     * @throws ConflictException cuando existe una colisión de unicidad
     * @throws RuntimeException  cuando ocurre un error inesperado al validar unicidad
     */
    private void validateProductUniqueness(
        String name,
        String partNumber,
        String slug,
        Long currentId
    ) {
        productRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe un producto registrado con el nombre '%s'.",
                    name
                ));
            }
        });

        productRepository.findBySlug(slug).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe un producto registrado con el nombre '%s'.",
                    name
                ));
            }
        });

        productRepository.findByPartNumber(partNumber).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "El número de parte '%s' ya está registrado.",
                    partNumber
                ));
            }
        });
    }

    /**
     * Válida la coherencia entre precio promocional y precio original.
     *
     * @param price         precio de venta
     * @param originalPrice precio original de referencia
     * @throws ConflictException cuando el precio no es menor que el original
     * @throws RuntimeException  cuando ocurre un error inesperado al validar precios
     */
    private void validatePricePromotion(BigDecimal price, BigDecimal originalPrice) {
        if (originalPrice == null) {
            return;
        }
        if (price == null) {
            throw new ConflictException(
                "El precio de venta es obligatorio cuando existe un precio original."
            );
        }
        if (price.compareTo(originalPrice) >= 0) {
            throw new ConflictException(
                "El precio de venta debe ser menor que el precio original."
            );
        }
    }

    /**
     * Válida la coherencia entre stock disponible y estado de visibilidad.
     *
     * @param stock  unidades disponibles del producto
     * @param status estado actual del producto
     * @throws ConflictException cuando el producto se marca ACTIVO sin stock
     * @throws RuntimeException  cuando ocurre un error inesperado al validar el stock
     */
    private void validateStockStatus(Integer stock, ProductStatus status) {
        if (stock == null || status == null) {
            return;
        }
        if (stock == 0 && status == ProductStatus.ACTIVO) {
            throw new ConflictException(
                "No se puede marcar como ACTIVO un producto sin stock disponible."
            );
        }
    }
}
