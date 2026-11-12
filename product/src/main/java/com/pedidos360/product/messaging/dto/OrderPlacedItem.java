package com.pedidos360.product.messaging.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderPlacedItem(
        @NotBlank(message = "productId es obligatorio") String productId,
        String name,
        @Min(value = 1, message = "quantity debe ser mayor a 0") int quantity,
        @PositiveOrZero(message = "subtotal no puede ser negativo") double subtotal
) {}