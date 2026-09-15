package com.pedidos360.product.mapper;

import com.pedidos360.product.dto.ProductRequestDTO;
import com.pedidos360.product.dto.ProductResponseDTO;
import com.pedidos360.product.model.Product;

public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();
        product.setCode(dto.code());
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        product.setCategory(dto.category());
        product.setImageUrl(dto.imageUrl());
        return product;
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory(),
                product.getImageUrl()
        );
    }
}