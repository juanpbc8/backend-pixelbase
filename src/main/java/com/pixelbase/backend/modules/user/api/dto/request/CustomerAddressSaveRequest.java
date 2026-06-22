package com.pixelbase.backend.modules.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerAddressSaveRequest(
    @NotBlank(message = "La dirección exacta es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    String addressLine,

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100, message = "El departamento no puede exceder los 100 caracteres")
    String department,

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100, message = "La provincia no puede exceder los 100 caracteres")
    String province,

    @NotBlank(message = "El distrito es obligatorio")
    @Size(max = 100, message = "El distrito no puede exceder los 100 caracteres")
    String district,

    @Size(max = 255, message = "La referencia no puede exceder los 255 caracteres")
    String reference,

    @NotNull(message = "Debe especificar si la dirección es la predeterminada")
    Boolean isDefault // Usamos wrapper Boolean para permitir validación física con @NotNull
) {
}
