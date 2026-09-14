package com.pedidos360.cart.dto;

public record CartItemResponseDTO(
        String productId,
        String name,
        double price,
        int quantity,
        double subtotal
) {}