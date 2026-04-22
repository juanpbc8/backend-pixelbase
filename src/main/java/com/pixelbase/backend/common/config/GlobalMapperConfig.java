package com.pixelbase.backend.common.config;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración centralizada para MapStruct.
 * Todos los mappers del proyecto heredarán estos comportamientos.
 */
@MapperConfig(
        // Genera la implementación como un @Component de Spring.
        // Permite inyectar el mapper en tus servicios usando @Autowired o constructor.
        componentModel = "spring",

        // Si el DTO tiene campos que la Entidad no (o viceversa), MapStruct no lanzará error.
        unmappedTargetPolicy = ReportingPolicy.IGNORE,

        // Prioriza el uso de métodos 'addSomething()' en lugar de 'setList()'.
        // CRÍTICO para JPA: asegura que al agregar un ítem a un pedido (Order),
        // se establezca correctamente la relación bidireccional (item.setOrder(this)).
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,

        // Usa inyección por constructor para otros mappers (definidos en 'uses').
        // Es la recomendación oficial de Spring: facilita pruebas unitarias y
        // ayuda a detectar dependencias circulares en tiempo de arranque.
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,

        // Lanza una advertencia (Warning) si MapStruct hace una conversión automática dudosa.
        // Ej: Convertir un String "199.99" de un JSON a un Double de base de datos sin validación previa.
        typeConversionPolicy = ReportingPolicy.WARN
)
public interface GlobalMapperConfig {
    // Esta interfaz no necesita métodos; sirve como plantilla de configuración
    // para todos los mappers del proyecto que usen @Mapper(config = GlobalMapperConfig.class)
}