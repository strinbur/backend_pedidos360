package com.pedidos360.cart.dto;

import java.util.List;

public record CartResponseDTO(
        List<CartItemResponseDTO> items,
        double total
) {}