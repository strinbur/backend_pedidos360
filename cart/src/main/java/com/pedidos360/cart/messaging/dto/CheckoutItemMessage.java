package com.pedidos360.cart.messaging.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CheckoutItemMessage(
        @NotBlank(message = "productId es obligatorio") String productId,
        @Min(value = 1, message = "quantity debe ser mayor a 0") int quantity
) {}