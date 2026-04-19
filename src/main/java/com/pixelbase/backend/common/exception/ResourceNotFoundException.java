package com.pixelbase.backend.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor Pro: throw new ResourceNotFoundException("Producto", 10);
    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s con ID %s no fue encontrado", resource, id));
    }
}