package com.pedidos360.cart.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderResultMessage(
        @NotBlank(message = "cartId es obligatorio") String cartId,
        @NotNull(message = "status es obligatorio") OrderResultStatus status,
        String reason,
        String occurredAt,
        @NotEmpty(message = "items no puede estar vacío") List<@Valid OrderResultItem> items
) {}