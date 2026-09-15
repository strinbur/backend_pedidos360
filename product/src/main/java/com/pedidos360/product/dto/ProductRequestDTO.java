package com.pedidos360.product.dto;

public record ProductRequestDTO(
        String code,
        String name,
        String description,
        double price,
        int stock,
        String category,
        String imageUrl
) {}