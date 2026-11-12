package com.pedidos360.cart.messaging.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockLowMessage(
        @NotBlank(message = "productId es obligatorio") String productId,
        String code,
        String name,
        @Min(value = 0, message = "stock no puede ser negativo") int stock,
        @Min(value = 0, message = "threshold no puede ser negativo") int threshold
) {}