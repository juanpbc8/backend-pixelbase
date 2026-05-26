package com.pixelbase.backend.common.storage.controller;

import com.pixelbase.backend.common.storage.dto.ImageUploadResponse;
import com.pixelbase.backend.common.storage.service.IImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
            "su ID."
    )
    @ApiResponse(responseCode = "201", description = "Imagen subida y optimizada con éxito en la nube.")
    public ResponseEntity<ImageUploadResponse> upload(
        @Parameter(description = "Archivo binario de la imagen (jpeg, png, webp)")
        @RequestParam("file") MultipartFile file,

        @Parameter(description = "Subcarpeta de destino en el bucket (ej: products, brands, users)")
        @RequestParam(value = "folder", defaultValue = "products") String folder
    ) {
        ImageUploadResponse response = storageService.upload(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping
    @Operation(
        summary = "Eliminar una imagen de la nube de forma quirúrgica",
        description = "Remueve permanentemente un archivo de Cloudinary para evitar almacenamiento huérfano."
    )
    @ApiResponse(responseCode = "204", description = "Imagen purgada del servidor cloud con éxito.")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Identificador público completo provisto por Cloudinary", example =
            "pixelbase/products/id")
        @RequestParam("publicId") String publicId
    ) {
        storageService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}
