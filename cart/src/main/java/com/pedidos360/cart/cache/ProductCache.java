package com.pedidos360.cart.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductCache {

    private final Map<String, CachedProduct> products = new ConcurrentHashMap<>();

    public void put(CachedProduct product) {
        products.put(product.id(), product);
    }

    public Optional<CachedProduct> find(String productId) {
        return Optional.ofNullable(products.get(productId));
    }

    public void remove(String productId) {
        products.remove(productId);
    }
}