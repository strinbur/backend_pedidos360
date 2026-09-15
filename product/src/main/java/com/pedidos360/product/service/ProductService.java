package com.pedidos360.product.service;

import com.pedidos360.product.dto.ProductRequestDTO;
import com.pedidos360.product.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO create(ProductRequestDTO dto);

    List<ProductResponseDTO> findAll();

    ProductResponseDTO findById(String id);

    ProductResponseDTO update(String id, ProductRequestDTO dto);

    void delete(String id);

    ProductResponseDTO findByCode(String code);
}