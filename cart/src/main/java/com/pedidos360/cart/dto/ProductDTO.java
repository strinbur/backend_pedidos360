package com.pedidos360.cart.dto;

public record ProductDTO(
        String id,
        String code,
        String name,
        String description,
        double price,
        int stock,
        String category,
        String imageUrl
) {}