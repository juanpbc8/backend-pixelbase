package com.pixelbase.backend.modules.catalog.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.dto.request.BrandRequest;
import com.pixelbase.backend.modules.catalog.repository.BrandRepository;
import com.pixelbase.backend.modules.catalog.service.IBrandService;
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

    private final IBrandService brandService;
    private final BrandRepository brandRepository;

    @Override
    public void seed() {
        if (brandRepository.count() > 0) return;

        List<BrandRequest> brands = List.of(
            new BrandRequest("Logitech",
                "https://res.cloudinary.com/pixelbase/brands/logitech.png"),
            new BrandRequest("ASUS ROG",
                "https://res.cloudinary.com/pixelbase/brands/asus.png"),
            new BrandRequest("Razer",
                "https://res.cloudinary.com/pixelbase/brands/razer.png"),
            new BrandRequest("Corsair",
                "https://res.cloudinary.com/pixelbase/brands/corsair.png"),
            new BrandRequest("Kingston FURY",
                "https://res.cloudinary.com/pixelbase/brands/kingston.png")
        );

        brands.forEach(brandService::create);
        log.info(" ✅ -> BrandSeeder: {} marcas registradas con slugs automáticos.", brands.size());
    }
}
