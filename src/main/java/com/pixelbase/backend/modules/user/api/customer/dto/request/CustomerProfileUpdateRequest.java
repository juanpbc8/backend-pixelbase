package com.pixelbase.backend.modules.user.api.customer.dto.request;

import com.pixelbase.backend.common.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerProfileUpdateRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    String firstName,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder los 100 caracteres")
    String lastName,

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Size(max = 30, message = "El teléfono no puede exceder los 30 caracteres")
    @Pattern(regexp = "^\\d{9,12}$", message = "El teléfono debe contener entre 9 y 12 dígitos numéricos")
    String phone,

    @NotNull(message = "El tipo de documento es obligatorio")
    DocumentType documentType,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z]{8,20}$", message = "El número de documento contiene caracteres " +
        "inválidos o longitud incorrecta")
    String documentNumber
) {
}
