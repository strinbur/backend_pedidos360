package com.pedidos360.cart.usecase;

import com.pedidos360.cart.cache.ProductCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RemoveProductFromCache {

    private static final Logger log = LoggerFactory.getLogger(RemoveProductFromCache.class);
    private final ProductCache cache;

    public RemoveProductFromCache(ProductCache cache) {
        this.cache = cache;
    }

    public void execute(String productId) {
        cache.remove(productId);
        log.info("Producto eliminado de la caché productId={}", productId);
    }
}