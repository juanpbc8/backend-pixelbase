package com.pixelbase.backend.modules.configuration.seed;

import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.configuration.domain.StoreEntity;
import com.pixelbase.backend.modules.configuration.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(6)
@RequiredArgsConstructor
@Slf4j
public class StoreSeeder implements DataSeeder {
    private final StoreRepository storeRepository;

    @Override
    public void seed() {
        if (storeRepository.count() > 0) return;

        List<StoreEntity> stores = List.of(
            StoreEntity.builder()
                .name("Tienda Principal PixelBase")
                .addressLine("Av. Bolivia N° 148 - CC. CENTRO LIMA")
                .department("Lima")
                .province("Lima")
                .district("Cercado de Lima")
                .active(true)
                .build(),
            StoreEntity.builder()
                .name("Tienda Secundaria PixelBase")
                .addressLine("Av. Inca Garcilaso de la Vega N° 1251 - CC. COMPUPLAZA")
                .department("Lima")
                .province("Lima")
                .district("Cercado de Lima")
                .active(true)
                .build()
        );

        storeRepository.saveAll(stores);
    }
}
