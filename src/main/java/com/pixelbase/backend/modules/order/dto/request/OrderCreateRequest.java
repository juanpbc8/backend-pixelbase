package com.pixelbase.backend.modules.order.dto.request;

import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.modules.order.domain.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Contrato único para la creación de pedidos en el Storefront de Pixelbase.
 * Contiene todas las estructuras internas necesarias para el flujo de Checkout.
 */
public record OrderCreateRequest(

    @NotNull(message = "El tipo de entrega es obligatorio")
    DeliveryType deliveryType,

    @Positive(message = "El id de la tienda debe ser un número positivo")
    Long storeId,

    @NotNull(message = "Los datos del comprador son obligatorios")
    @Valid
    CustomerRequest customer,

    @NotNull(message = "La dirección logística es obligatoria")
    @Valid
    AddressRequest address,

    @NotNull(message = "Los datos del receptor son obligatorios")
    @Valid
    RecipientRequest recipient,

    @NotEmpty(message = "La orden debe contener al menos un producto")
    @Valid
    List<OrderItemRequest> items
) {

    /**
     * Snapshot de los datos personales y fiscales de quien realiza el pago.
     */
    public record CustomerRequest(
        @NotBlank(message = "El nombre del cliente no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido del cliente no puede estar vacío")
        @Size(max = 100, message = "El apellido no puede exceder los 100 caracteres")
        String lastName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico es inválido")
        String email,

        @NotBlank(message = "El teléfono del cliente es obligatorio")
        @Pattern(regexp = "^\\d{9,12}$", message = "El teléfono debe contener entre 9 y 12 dígitos " +
            "numéricos")
        String phone,

        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType docType,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(min = 8, max = 20, message = "El documento debe tener entre 8 y 20 caracteres")
        String docNumber
    ) {
    }

    /**
     * Datos logísticos del destino del paquete (Domicilio o Tienda física).
     */
    public record AddressRequest(
        @NotBlank(message = "La dirección exacta es obligatoria")
        @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
        String addressLine,

        @NotBlank(message = "El departamento es obligatorio")
        String department,

        @NotBlank(message = "La provincia es obligatoria")
        String province,

        @NotBlank(message = "El distrito es obligatorio")
        String district,

        @Size(max = 255, message = "La referencia no puede exceder los 255 caracteres")
        String reference
    ) {
    }

    /**
     * Datos de la persona autorizada a recibir el envío o recoger el hardware en mostrador.
     */
    public record RecipientRequest(
        @NotBlank(message = "El nombre del receptor no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido del receptor no puede estar vacío")
        @Size(max = 100, message = "El apellido no puede exceder los 100 caracteres")
        String lastName,

        @NotBlank(message = "El teléfono del receptor es obligatorio")
        @Pattern(regexp = "^\\d{9,12}$", message = "El teléfono del receptor debe contener entre 9 y 12 " +
            "dígitos")
        String phone,

        @NotNull(message = "El tipo de documento del receptor es obligatorio")
        DocumentType docType,

        @NotBlank(message = "El número de documento del receptor es obligatorio")
        @Size(min = 8, max = 20, message = "El documento del receptor debe tener entre 8 y 20 caracteres")
        String docNumber
    ) {
    }

    /**
     * Representación de un producto en el carrito identificado por su Slug de catálogo.
     */
    public record OrderItemRequest(
        @NotBlank(message = "El slug del producto es obligatorio")
        String productSlug,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima permitida de compra es 1 unidad")
        @Max(value = 10, message = "Por políticas de stock, el máximo permitido son 10 unidades por producto")
        Integer quantity
    ) {
    }
}
