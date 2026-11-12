package com.pedidos360.product.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record OrderPlacedMessage(
        @NotBlank(message = "cartId es obligatorio") String cartId,
        @PositiveOrZero(message = "total no puede ser negativo") double total,
        String occurredAt,
        @NotEmpty(message = "items no puede estar vacío") List<@Valid OrderPlacedItem> items
) {}