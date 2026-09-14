package com.pedidos360.product.service.impl;

import com.pedidos360.product.dto.ProductRequestDTO;
import com.pedidos360.product.dto.ProductResponseDTO;
import com.pedidos360.product.exception.DuplicateProductCodeException;
import com.pedidos360.product.exception.ProductNotFoundException;
import com.pedidos360.product.mapper.ProductMapper;
import com.pedidos360.product.model.Product;
import com.pedidos360.product.repository.ProductRepository;
import com.pedidos360.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        if (productRepository.existsByCode(dto.code())) {
            throw new DuplicateProductCodeException(dto.code());
        }
        Product product = ProductMapper.toEntity(dto);
        Product saved = productRepository.save(product);
        return ProductMapper.toResponseDTO(saved);
    }

    @Override
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponseDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));
        return ProductMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO update(String id, ProductRequestDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con id: " + id));

        if (productRepository.existsByCodeAndIdNot(dto.code(), id)) {
            throw new DuplicateProductCodeException(dto.code());
        }

        existing.setCode(dto.code());
        existing.setName(dto.name());
        existing.setDescription(dto.description());
        existing.setPrice(dto.price());
        existing.setStock(dto.stock());
        existing.setCategory(dto.category());
        existing.setImageUrl(dto.imageUrl());

        Product updated = productRepository.save(existing);
        return ProductMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado con id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponseDTO findByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con code: " + code));
        return ProductMapper.toResponseDTO(product);
    }
}