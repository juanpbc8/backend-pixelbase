package com.pixelbase.backend.common.storage.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.storage.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageService implements IImageStorageService {

    private final Cloudinary cloudinary;

    @Override
    public ImageUploadResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No se puede subir un archivo vacío o nulo.");
        }

        try {
            // Configuramos las opciones de carga basadas en la documentación del SDK
            Map<?, ?> options = ObjectUtils.asMap(
                "folder", "pixelbase/" + folder, // Organiza los archivos en carpetas por módulo
                "resource_type", "image", // Detecta automáticamente si es imagen, video o raw
                "use_filename", true, // Mantiene el nombre original del archivo
                "unique_filename", true, // Añade un sufijo aleatorio para evitar colisiones
                "overwrite", true, // Permite reemplazar el archivo si el ID es idéntico

                // Transformación Entrante (optimización en tiempo de subida):
                // c_limit: No estira imágenes, solo encoge si superan las dimensiones.
                // w_800: Establece un ancho máximo de 800px para balancear nitidez y peso.
                // q_auto: Ajusta la compresión automáticamente para mantener calidad visual.
                // f_auto: Selecciona el formato más eficiente según el navegador (ej.: WebP o AVIF).
                "transformation", "c_limit,w_800,q_auto,f_auto"
            );

            // Subimos el arreglo de bytes directamente a la API de Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            return new ImageUploadResponse(secureUrl, publicId);
        } catch (IOException e) {
            throw new RuntimeException(
                "Error crítico de I/O al procesar los bytes de la imagen para Cloudinary.", e
            );
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("El publicId no puede estar vacío para ejecutar el borrado.");
        }

        try {
            // Solicitamos la destrucción inmediata del recurso multimedia en la nube
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            if (!"ok".equals(result.get("result"))) {
                throw new ConflictException(
                    "La imagen con el ID proporcionado no existe en Cloudinary.");
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Error crítico al intentar eliminar el activo físico en Cloudinary: " + publicId, e
            );
        }
    }
}
