package com.pedidos360.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CartItemRequestDTO(
        @NotBlank(message = "El productId es obligatorio")
        String productId,

        @Positive(message = "La cantidad debe ser mayor a 0")
        int quantity
) {}