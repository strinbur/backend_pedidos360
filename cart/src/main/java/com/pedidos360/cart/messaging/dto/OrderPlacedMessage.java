package com.pedidos360.cart.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderPlacedMessage(
        @NotBlank(message = "cartId es obligatorio") String cartId,
        double total,
        String occurredAt,
        @NotEmpty(message = "items no puede estar vacío") List<@Valid OrderPlacedItem> items
) {}