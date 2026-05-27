package com.pixelbase.backend.common.storage.controller;

import com.pixelbase.backend.common.storage.dto.ImageUploadResponse;
import com.pixelbase.backend.common.storage.service.IImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/images")
@RequiredArgsConstructor
@Tag(name = "Media Infrastructure API", description = "Endpoints globales para la gestión de activos y " +
    "archivos")
public class ImageUploadController {

    private final IImageStorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir una imagen de forma asíncrona",
        description = "Recibe un binario, lo procesa y lo aloja en Cloudinary devolviendo la URL segura y " +
            "su publicId. Ademas es necesario mandar un title (nombre del producto o recurso) para SEO"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Imagen alojada, transformada a .webp y optimizada con éxito en la nube."),
        @ApiResponse(
            responseCode = "400",
            description = "El archivo excede el límite de tamaño permitido por el sistema")
    })
    public ResponseEntity<ImageUploadResponse> upload(
        @Parameter(description = "Archivo binario de la imagen (jpeg, png, webp)")
        @RequestParam("file") MultipartFile file,

        @Parameter(description = "Subcarpeta de destino en el bucket (ej: products, brands, users)")
        @RequestParam(value = "folder", defaultValue = "products") String folder,

        @Parameter(
            description = "Nombre del producto en texto plano (se usará como base semántica para la URL)",
            example = "Teclado Logitech G513 Carbon"
        )
        @RequestParam(value = "title") String title
    ) {
        ImageUploadResponse response = storageService.upload(file, folder, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping
    @Operation(
        summary = "Eliminar una imagen de la nube de forma quirúrgica",
        description = "Remueve permanentemente un archivo de Cloudinary para evitar almacenamiento huérfano."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Imagen purgada del servidor cloud con éxito."),
        @ApiResponse(
            responseCode = "409",
            description = "La imagen con el publicId proporcionado no existe o ya fue eliminada."
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identificador público completo provisto por Cloudinary", example =
            "pixelbase/products/teclado-logitech-g513-carbon_7cae59")
        @RequestParam("publicId") String publicId
    ) {
        storageService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
