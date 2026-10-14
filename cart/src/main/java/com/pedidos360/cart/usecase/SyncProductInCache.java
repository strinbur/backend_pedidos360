package com.pedidos360.cart.usecase;

import com.pedidos360.cart.cache.CachedProduct;
import com.pedidos360.cart.cache.ProductCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SyncProductInCache {

    private static final Logger log = LoggerFactory.getLogger(SyncProductInCache.class);
    private final ProductCache cache;

    public SyncProductInCache(ProductCache cache) {
        this.cache = cache;
    }

    public void execute(CachedProduct product) {
        if (product.name() == null || product.name().isBlank()) {
            throw new IllegalArgumentException("El evento UPDATED no trae nombre");
        }
        if (product.price() < 0) {
            throw new IllegalArgumentException("El evento UPDATED trae un precio inválido");
        }
        cache.put(product);
        log.info("Caché actualizada productId={} nombre={} precio={}",
                product.id(), product.name(), product.price());
    }
}