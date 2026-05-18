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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Obtiene el árbol completo de categorías (con sus hijos) optimizado mediante JOIN FETCH
     * para evitar N+1 queries.
     */
    @Override
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findAllWithChildren().stream()
            .map(categoryMapper::toResponse)
            .toList();
    }

    /**
     * Recupera una categoría (y sus subcategorías) a partir de su slug.
     *
     * @param slug identificador legible de la categoría
     * @throws ResourceNotFoundException si no existe una categoría con ese slug
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
     * @throws ResourceNotFoundException si no existe una categoría con ese id
     */
    @Override
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(categoryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con ID %d.",
                id))));
    }

    /**
     * Obtiene la lista de categorías con la estructura utilizada por la vista de administración.
     */
    @Override
    public List<CategoryAdminTableResponse> getAdminTable() {
        return categoryRepository.findAdminTable();
    }

    /**
     * Crea una nueva categoría a partir de la petición proporcionada.
     * Genera el slug automáticamente a partir del nombre y válida su unicidad.
     * Si se especifica una categoría padre, verifica que exista y que sea válida
     * según las reglas de profundidad y asociación de productos.
     *
     * @param request datos de la categoría a crear
     * @throws ConflictException         si el slug generado ya existe en otra categoría
     * @throws ResourceNotFoundException si la categoría padre indicada no existe
     * @throws BadRequestException       si la relación padre/hijo viola las reglas de negocio
     */
    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String generatedSlug = SlugUtil.toSlug(request.name());
        validateSlugUniqueness(generatedSlug, null);

        CategoryEntity category = categoryMapper.toEntity(request);
        category.setSlug(generatedSlug); // Slug automático

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
     * Actualiza una categoría existente identificada por su id con los nuevos datos.
     * <p>
     * Genera y valida el slug resultante; permite reasignar la categoría padre o
     * eliminarlo. Se comprueba la existencia del recurso y la validez de la nueva
     * jerarquía (profundidad y no asignación a descendientes).
     *
     * @param id      identificador de la categoría a actualizar
     * @param request nuevos datos de la categoría
     * @throws ResourceNotFoundException si no existe la categoría a actualizar o el padre indicado
     * @throws ConflictException         si el slug generado ya pertenece a otra categoría
     * @throws BadRequestException       si la nueva jerarquía viola las reglas de negocio
     */
    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con ID %d.", id
            )));

        String generatedSlug = SlugUtil.toSlug(request.name());
        validateSlugUniqueness(generatedSlug, id);

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

        category.setName(request.name());
        category.setSlug(generatedSlug);

        return categoryMapper.toResponse(category);
    }

    /**
     * Elimina la categoría identificada por id después de validar que no tenga
     * subcategorías activas ni productos asignados directamente.
     *
     * @param id identificador de la categoría a eliminar
     * @throws ResourceNotFoundException si no existe la categoría
     * @throws BadRequestException       si la categoría tiene subcategorías o productos asignados
     */
    @Override
    @Transactional
    public void delete(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(String.format(
                "No existe una categoría registrada con ID %d.", id
            )));

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
     * Válida que una categoría padre sea adecuada para asociarla como padre de la
     * categoría indicada. Comprueba que la asignación no exceda la profundidad
     * máxima permitida y que el padre no tenga productos asignados que impidan
     * la creación de subcategorías.
     *
     * @param category categoría que se va a asociar como hija
     * @param parent   categoría candidata a padre
     * @throws BadRequestException si la nueva jerarquía supera la profundidad
     *                             máxima o si el padre ya tiene productos
     *                             asociados
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
     * la categoría proporcionada. Devuelve 0 si no tiene subcategorías.
     *
     * @param category categoría raíz del subárbol a calcular
     */
    // Auxiliar recursivo de soporte para calcular la altura del árbol descendiente
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
     * descendiente de ésta, evitando ciclos en la jerarquía.
     *
     * @param category  categoría que se está moviendo/actualizando
     * @param newParent nueva categoría padre propuesta
     * @throws BadRequestException si se intenta asignar la categoría como su
     *                             propio padre o a uno de sus descendientes
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
     * Resuelve el nivel (profundidad) de la categoría dentro del árbol. Las
     * raíces tienen nivel 1; cada nivel de anidamiento aumenta el contador en 1.
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
     * Válida la unicidad del slug entre las categorías. Si currentId es nulo se
     * verifica que ningún registro existente use el slug; si no es nulo se
     * permite que el registro con el mismo id conserve el slug.
     *
     * @param slug      slug a validar
     * @param currentId id del registro actual (puede ser nulo para nuevas
     *                  categorías)
     * @throws ConflictException si existe otra categoría con el mismo slug
     */
    private void validateSlugUniqueness(String slug, Long currentId) {
        if (currentId == null) {
            if (categoryRepository.existsBySlug(slug)) {
                throw new ConflictException(String.format(
                    "Ya existe una categoría registrada con el nombre o slug idéntico a '%s'.", slug
                ));
            }
            return;
        }

        CategoryEntity existing = categoryRepository.findBySlug(slug)
            .orElse(null);

        if (existing != null && !existing.getId().equals(currentId)) {
            throw new ConflictException(String.format(
                "Ya existe una categoría registrada con el nombre o slug idéntico a '%s'.", slug
            ));
        }
    }
}
