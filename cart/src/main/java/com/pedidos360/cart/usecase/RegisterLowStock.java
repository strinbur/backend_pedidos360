package com.pedidos360.cart.usecase;

import com.pedidos360.cart.cache.LowStockRegistry;
import com.pedidos360.cart.messaging.dto.StockLowMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterLowStock {

    private static final Logger log = LoggerFactory.getLogger(RegisterLowStock.class);
    private final LowStockRegistry registry;

    public RegisterLowStock(LowStockRegistry registry) {
        this.registry = registry;
    }

    public void execute(StockLowMessage message) {
        if ("OK".equals(message.level())) {
            registry.remove(message.productId());
            log.info("Stock recuperado productId={} nombre={} stock={}",
                    message.productId(), message.name(), message.stock());
            return;
        }
        registry.put(message.productId(), message.stock());
        log.warn("Stock {} productId={} nombre={} stock={} (umbral={})",
                message.level(), message.productId(), message.name(),
                message.stock(), message.threshold());
    }
}