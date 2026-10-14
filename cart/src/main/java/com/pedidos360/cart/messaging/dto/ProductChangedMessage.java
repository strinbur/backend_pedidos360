package com.pedidos360.cart.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductChangedMessage(
        @NotBlank(message = "productId es obligatorio") String productId,
        @NotNull(message = "type es obligatorio") ProductChangeType type,
        String code,
        String name,
        Double price,
        String imageUrl
) {}