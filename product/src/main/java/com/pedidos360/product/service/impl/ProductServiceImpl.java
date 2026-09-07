package com.pedidos360.product.service.impl;

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
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    @Override
    public Product update(String id, Product product) {
        Product existing = findById(id);
        existing.setCode(product.getCode());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setCategory(product.getCategory());

        return productRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        findById(id);
        productRepository.deleteById(id);
    }

    @Override
    public Product findByCode(String code) {
        return productRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con code: " + code));
    }

}