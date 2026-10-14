package com.pedidos360.cart.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CheckoutMessage(
        @NotBlank(message = "cartId es obligatorio") String cartId,
        String userId,
        @NotEmpty(message = "items no puede estar vacío") List<@Valid CheckoutItemMessage> items
) {}