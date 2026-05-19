package com.pixelbase.backend.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class DatabaseInfrastructureSeeder implements DataSeeder {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void seed() {
        log.info(" 🛠️ Asegurando estructuras nativas de Postgres...");

        // Garantizamos la secuencia antes de que cualquier producto intente usarla
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS product_sku_seq START WITH 1 INCREMENT BY 1;");
        log.info(" ✅ -> InfrastructureSeeder: Secuencia 'product_sku_seq' lista.");
    }
}
