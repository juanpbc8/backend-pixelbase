package com.pixelbase.backend.common.storage.service;

import com.pixelbase.backend.common.storage.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IImageStorageService {

    /**
     * Procesa un archivo binario multipart, aplicando transformaciones de optimización,
     * y lo almacena en el directorio especificado de la nube.
     *
     * @param file   El archivo binario recibido desde la petición HTTP multipart.
     * @param folder El subdirectorio destino dentro del contenedor (ej.: "products", "brands").
     * @return Un ImageUploadResponse con los metadatos y la URL final de persistencia.
     */
    ImageUploadResponse upload(MultipartFile file, String folder);

    /**
     * Elimina de forma asíncrona o directa un recurso multimedia del servidor de la nube
     * utilizando su identificador único de persistencia.
     *
     * @param publicId El identificador único (clave primaria) con el que se registró el archivo.
     */
    void delete(String publicId);
}
