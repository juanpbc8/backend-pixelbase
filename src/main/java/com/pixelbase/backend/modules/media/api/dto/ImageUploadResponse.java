package com.pixelbase.backend.modules.media.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar tras la subida asíncrona y exitosa de una imagen")
public record ImageUploadResponse(
    @Schema(
        description = "URL pública de acceso seguro HTTPS optimizada para el renderizado nativo en la UI",
        example = "https://res.cloudinary.com/pixelbase/image/upload/v1716/products/teclado-g513.webp"
    )
    String url,

    @Schema(
        description = "Identificador único del archivo en el servidor de almacenamiento, necesario para " +
            "ejecuciones de borrado",
        example = "pixelbase/products/teclado-g513_u89wqa"
    )
    String publicId
) {
}
