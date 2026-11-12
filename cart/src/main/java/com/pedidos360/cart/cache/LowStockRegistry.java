package com.pedidos360.cart.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LowStockRegistry {

    private final Map<String, Integer> stocks = new ConcurrentHashMap<>();

    public void put(String productId, int stock) {
        stocks.put(productId, stock);
    }

    public Optional<Integer> find(String productId) {
        return Optional.ofNullable(stocks.get(productId));
    }

    public void remove(String productId) {
        stocks.remove(productId);
    }
}