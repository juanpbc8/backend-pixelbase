package com.pixelbase.backend.common.seed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

@Slf4j
@Configuration
@Profile("dev")
public class DatabaseSeederRunner {
    @Bean
    CommandLineRunner init(List<DataSeeder> seeders) {
        return args -> {
            if (seeders.isEmpty()) {
                log.warn("⚠️ No se encontraron DataSeeders para ejecutar.");
                return;
            }

            log.info("🌱 Pixelbase Seed System: Cargando módulos...");

            seeders.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(DataSeeder::seed);

            log.info("✨ Pixelbase Seed System: Datos listos para la demo.");
        };
    }
}
