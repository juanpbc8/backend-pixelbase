package com.pixelbase.backend.modules.catalog.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.api.admin.dto.request.BrandCreateRequest;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.service.BrandInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class BrandSeeder implements DataSeeder {

    private final BrandInternalService brandInternalService;
    private final BrandRepository brandRepository;

    @Override
    public void seed() {
        if (brandRepository.count() > 0) return;

        List<BrandCreateRequest> brands = List.of(
            new BrandCreateRequest("Logitech",
                "https://res.cloudinary.com/pixelbase/brands/logitech.png"),
            new BrandCreateRequest("ASUS ROG",
                "https://res.cloudinary.com/pixelbase/brands/asus.png"),
            new BrandCreateRequest("Razer",
                "https://res.cloudinary.com/pixelbase/brands/razer.png"),
            new BrandCreateRequest("Corsair",
                "https://res.cloudinary.com/pixelbase/brands/corsair.png"),
            new BrandCreateRequest("Kingston FURY",
                "https://res.cloudinary.com/pixelbase/brands/kingston.png")
        );

        brands.forEach(brandInternalService::create);
        log.info(" ✅ -> BrandSeeder: {} marcas registradas con slugs automáticos.", brands.size());
    }
}
