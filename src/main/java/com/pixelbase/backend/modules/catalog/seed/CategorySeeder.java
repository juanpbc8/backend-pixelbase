package com.pixelbase.backend.modules.catalog.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.dto.request.CategoryRequest;
import com.pixelbase.backend.modules.catalog.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import com.pixelbase.backend.modules.catalog.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(2)
@RequiredArgsConstructor
public class CategorySeeder implements DataSeeder {

    private final ICategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @Override
    public void seed() {
        if (categoryRepository.count() > 0) return;

        // Categorías padre
        CategoryResponse componentes = categoryService.create(new CategoryRequest("Componentes", null));
        CategoryResponse perifericos = categoryService.create(new CategoryRequest("Periféricos", null));
        CategoryResponse laptops = categoryService.create(new CategoryRequest("Laptops", null));
        CategoryResponse monitores = categoryService.create(new CategoryRequest("Monitores", null));

        // Sub de Componentes
        categoryService.create(new CategoryRequest("Procesadores", componentes.id()));
        categoryService.create(new CategoryRequest("Tarjetas de Video", componentes.id()));
        categoryService.create(new CategoryRequest("Memorias RAM", componentes.id()));

        // Sub de Periféricos
        categoryService.create(new CategoryRequest("Mouses Gamer", perifericos.id()));
        categoryService.create(new CategoryRequest("Teclados Mecánicos", perifericos.id()));
        categoryService.create(new CategoryRequest("Audífonos", perifericos.id()));

        // Sub de Laptops
        categoryService.create(new CategoryRequest("Laptops Gamer", laptops.id()));
        categoryService.create(new CategoryRequest("Laptops de Oficina", laptops.id()));

        // Sub de Monitores
        categoryService.create(new CategoryRequest("Monitores 144Hz+", monitores.id()));
        categoryService.create(new CategoryRequest("Monitores Ultrawide", monitores.id()));

        log.info(" ✅ -> CategorySeeder: Jerarquía de categorías creada con éxito.");
    }
}
