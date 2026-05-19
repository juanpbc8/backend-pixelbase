package com.pixelbase.backend.modules.catalog.service;

import com.pixelbase.backend.common.exception.BadRequestException;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.common.util.SlugUtil;
import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryAdminTableResponse;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.mapper.CategoryMapper;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Obtiene el árbol completo de categorías con sus hijos para alimentar la
     * navegación pública y evitar el problema de N+1 consultas.
     */
    @Override
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findAllWithChildren().stream()
            .map(categoryMapper::toResponse)
            .toList();
    }

    /**
     * Recupera una categoría y sus subcategorías a partir de su slug público.
     *
     * @param slug identificador legible de la categoría
     * @throws ResourceNotFoundException cuando no existe una categoría con ese slug
     */
    @Override
    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findBySlugWithChildren(slug)
            .map(categoryMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con el slug '%s'.",
                slug)));
    }

    /**
     * Recupera una categoría por su identificador numérico.
     *
     * @param id identificador de la categoría
     * @throws ResourceNotFoundException cuando no existe una categoría con ese identificador
     */
    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(findByIdOrThrow(id));
    }

    /**
     * Obtiene la lista plana de categorías utilizada por la vista de
     * administración.
     */
    @Override
    public List<CategoryAdminTableResponse> getAdminTable() {
        return categoryRepository.findAdminTable();
    }

    /**
     * Crea una nueva categoría, genera su slug automáticamente y válida la
     * unicidad junto con las reglas de jerarquía.
     *
     * @param request datos de la categoría a crear
     * @throws ConflictException         cuando el nombre o el slug ya pertenecen a otra categoría registrada
     * @throws ResourceNotFoundException cuando la categoría padre indicada no existe
     * @throws BadRequestException       cuando la relación padre/hijo viola las reglas de negocio
     */
    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String generatedSlug = SlugUtil.toSlug(request.name());
        validateCategoryUniqueness(request.name(), generatedSlug, null);

        CategoryEntity category = categoryMapper.toEntity(request);
        category.setSlug(generatedSlug);

        if (request.parentId() != null) {
            CategoryEntity parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                    "No existe la categoría padre con ID %d para asociar la nueva categoría.",
                    request.parentId())
                ));
            validateParentForChild(category, parent);
            category.setParent(parent);
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /**
     * Actualiza una categoría existente, recalcula el slug cuando cambie el
     * nombre y válida la jerarquía resultante.
     *
     * @param id      identificador de la categoría a actualizar
     * @param request nuevos datos de la categoría
     * @throws ResourceNotFoundException cuando no existe la categoría a actualizar o el padre indicado
     * @throws ConflictException         cuando el nombre o el slug colisionan con otra categoría registrada
     * @throws BadRequestException       cuando la nueva jerarquía viola las reglas de negocio
     */
    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        CategoryEntity category = findByIdOrThrow(id);

        String generatedSlug = SlugUtil.toSlug(request.name());
        validateCategoryUniqueness(request.name(), generatedSlug, id);

        if (request.parentId() == null) {
            category.setParent(null);
        } else if (category.getParent() == null || !request.parentId().equals(category.getParent().getId())) {
            CategoryEntity parent = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                    "No existe la categoría padre con ID %d para actualizar la jerarquía.",
                    request.parentId())
                ));
            validateParentForChild(category, parent);
            validateParentNotSelfOrDescendant(category, parent);

            category.setParent(parent);
        }

        categoryMapper.updateEntityFromRequest(request, category);
        category.setSlug(generatedSlug);

        return categoryMapper.toResponse(category);
    }

    /**
     * Elimina una categoría solo si no tiene subcategorías activas ni productos
     * asignados directamente.
     *
     * @param id identificador de la categoría a eliminar
     * @throws ResourceNotFoundException cuando no existe la categoría
     * @throws BadRequestException       cuando la categoría tiene subcategorías o productos asignados
     */
    @Override
    @Transactional
    public void delete(Long id) {
        CategoryEntity category = findByIdOrThrow(id);

        if (categoryRepository.existsByParentId(id)) {
            long subCategoryCount = categoryRepository.countByParentId(id);
            throw new BadRequestException(String.format(
                "No se puede eliminar la categoría '%s' (ID: %d) porque tiene %d subcategorías activas " +
                    "asociadas.",
                category.getName(),
                category.getId(),
                subCategoryCount
            ));
        }

        if (categoryRepository.hasProducts(id)) {
            long productCount = categoryRepository.countProductsByCategoryId(id);
            throw new BadRequestException(String.format(
                "No se puede eliminar la categoría '%s' (ID: %d) porque contiene %d productos asignados " +
                    "directos en el inventario.",
                category.getName(),
                category.getId(),
                productCount
            ));
        }

        categoryRepository.delete(category);
    }

    /**
     * Valida que una categoría padre sea adecuada para asociarla como padre de
     * la categoría indicada.
     *
     * @param category categoría que se va a asociar como hija
     * @param parent   categoría candidata a padre
     * @throws BadRequestException cuando la jerarquía supera la profundidad máxima
     *                             o el padre ya tiene productos asociados
     */
    private void validateParentForChild(CategoryEntity category, CategoryEntity parent) {
        int parentLevel = resolveLevel(parent);

        // Si la categoría que estamos moviendo ya es padre (tiene subcategorías), su altura es mayor.
        // Al tener hijos, ella actúa como nivel intermedio, por ende el nuevo padre NO PUEDE ser nivel 2.
        int subtreeHeight = calculateSubtreeHeight(category);

        if (parentLevel + subtreeHeight >= 3) {
            throw new BadRequestException(String.format(
                "La profundidad máxima de categorías es de 3 niveles." +
                    " La categoría '%s' (ID: %d) no puede ser hija de '%s' (ID: %d) porque supera el límite.",
                category.getName(),
                category.getId(),
                parent.getName(),
                parent.getId()
            ));
        }

        if (categoryRepository.hasProducts(parent.getId())) {
            long productCount = categoryRepository.countProductsByCategoryId(parent.getId());
            throw new BadRequestException(String.format(
                "No se puede crear una subcategoría bajo '%s' (ID: %d) porque ya cuenta con %d productos " +
                    "físicos asociados.",
                parent.getName(),
                parent.getId(),
                productCount
            ));
        }
    }

    /**
     * Calcula de forma recursiva la altura máxima del subárbol descendiente de
     * la categoría proporcionada.
     *
     * @param category categoría raíz del subárbol a calcular
     */
    private int calculateSubtreeHeight(CategoryEntity category) {
        if (category.getSubCategories() == null || category.getSubCategories().isEmpty()) {
            return 0;
        }
        int maxHeight = 0;
        for (CategoryEntity sub : category.getSubCategories()) {
            maxHeight = Math.max(maxHeight, calculateSubtreeHeight(sub));
        }
        return 1 + maxHeight;
    }

    /**
     * Verifica que la nueva categoría padre no sea la misma categoría ni un
     * descendiente suyo para evitar ciclos en la jerarquía.
     *
     * @param category  categoría que se está moviendo o actualizando
     * @param newParent nueva categoría padre propuesta
     * @throws BadRequestException cuando se intenta asignar la categoría como
     *                             su propio padre o a uno de sus descendientes
     */
    private void validateParentNotSelfOrDescendant(CategoryEntity category, CategoryEntity newParent) {
        if (category.getId().equals(newParent.getId())) {
            throw new BadRequestException(String.format(
                "Una categoría no puede ser su propio padre. Categoría '%s' (ID: %d).",
                category.getName(),
                category.getId()
            ));
        }

        CategoryEntity current = newParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new BadRequestException(String.format(
                    "No se puede asignar la categoría '%s' (ID: %d) a su propio descendiente '%s' (ID: %d).",
                    category.getName(),
                    category.getId(),
                    newParent.getName(),
                    newParent.getId()
                ));
            }
            current = current.getParent();
        }
    }

    /**
     * Resuelve el nivel de profundidad de la categoría dentro del árbol.
     *
     * @param category categoría cuya profundidad se desea conocer
     */
    private int resolveLevel(CategoryEntity category) {
        int level = 1;
        CategoryEntity current = category;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    /**
     * Válida de manera unificada la integridad del nombre y del slug para
     * evitar duplicados y conservar mensajes de conflicto legibles.
     *
     * @param name      nombre legible recibido en la petición
     * @param slug      slug generado o solicitado para la categoría
     * @param currentId identificador actual de la categoría; es nulo durante la creación
     * @throws ConflictException cuando el nombre o el slug ya pertenecen a otra categoría registrada
     */
    private void validateCategoryUniqueness(String name, String slug, Long currentId) {
        // 1. Control preventivo por nombre exacto (Case Insensitive)
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe una categoría registrada con el nombre '%s'.",
                    name
                ));
            }
        });

        // 2. Control preventivo de colisión por normalización de URL (Slug)
        categoryRepository.findBySlug(slug).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                throw new ConflictException(String.format(
                    "Ya existe una categoría registrada con un nombre equivalente o similar a '%s'.",
                    name
                ));
            }
        });
    }

    /**
     * Busca una categoría por identificador y falla de inmediato si no existe.
     *
     * @param id identificador numérico de la categoría
     * @throws ResourceNotFoundException cuando el registro no existe
     */
    private CategoryEntity findByIdOrThrow(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con ID %d.",
                id
            )));
    }
}
