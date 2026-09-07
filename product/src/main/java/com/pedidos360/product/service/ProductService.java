package com.pedidos360.product.service;

import com.pedidos360.product.model.Product;

import java.util.List;

public interface ProductService {

    Product create(Product product);

    List<Product> findAll();

    Product findById(String id);

    Product update(String id, Product product);

    void delete(String id);

    Product findByCode(String code);
}