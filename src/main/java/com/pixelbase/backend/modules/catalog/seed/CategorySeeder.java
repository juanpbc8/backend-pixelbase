package com.pixelbase.backend.modules.catalog.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.CategoryCreateRequest;
import com.pixelbase.backend.modules.catalog.api.shared.dto.response.CategoryResponse;
import com.pixelbase.backend.modules.catalog.repository.CategoryRepository;
import com.pixelbase.backend.modules.catalog.service.CategoryInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(2)
@RequiredArgsConstructor
public class CategorySeeder implements DataSeeder {

    private final CategoryInternalService categoryInternalService;
    private final CategoryRepository categoryRepository;

    @Override
    public void seed() {
        if (categoryRepository.count() > 0) return;

        // Categorías padre
        CategoryResponse componentes = categoryInternalService.create(new CategoryCreateRequest("Componentes",
            null));
        CategoryResponse perifericos = categoryInternalService.create(new CategoryCreateRequest("Periféricos",
            null));
        CategoryResponse laptops = categoryInternalService.create(new CategoryCreateRequest("Laptops", null));
        CategoryResponse monitores = categoryInternalService.create(new CategoryCreateRequest("Monitores",
            null));

        // Sub de Componentes
        categoryInternalService.create(new CategoryCreateRequest("Procesadores", componentes.id()));
        categoryInternalService.create(new CategoryCreateRequest("Tarjetas de Video", componentes.id()));
        categoryInternalService.create(new CategoryCreateRequest("Memorias RAM", componentes.id()));

        // Sub de Periféricos
        categoryInternalService.create(new CategoryCreateRequest("Mouses Gamer", perifericos.id()));
        categoryInternalService.create(new CategoryCreateRequest("Teclados Mecánicos", perifericos.id()));
        categoryInternalService.create(new CategoryCreateRequest("Audífonos", perifericos.id()));

        // Sub de Laptops
        categoryInternalService.create(new CategoryCreateRequest("Laptops Gamer", laptops.id()));
        categoryInternalService.create(new CategoryCreateRequest("Laptops de Oficina", laptops.id()));

        // Sub de Monitores
        categoryInternalService.create(new CategoryCreateRequest("Monitores 144Hz+", monitores.id()));
        categoryInternalService.create(new CategoryCreateRequest("Monitores Ultrawide", monitores.id()));

        log.info(" ✅ -> CategorySeeder: Jerarquía de categorías creada con éxito.");
    }
}
