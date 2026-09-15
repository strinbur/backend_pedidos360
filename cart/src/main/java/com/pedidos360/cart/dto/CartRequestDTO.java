package com.pedidos360.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartRequestDTO(
        @NotEmpty(message = "El carrito no puede estar vacío")
        @Valid
        List<CartItemRequestDTO> items
) {}