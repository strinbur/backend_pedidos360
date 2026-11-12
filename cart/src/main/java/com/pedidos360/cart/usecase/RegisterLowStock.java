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
        registry.put(message.productId(), message.stock());
        log.warn("Stock bajo productId={} nombre={} stock={} (umbral={})",
                message.productId(), message.name(), message.stock(), message.threshold());
    }
}