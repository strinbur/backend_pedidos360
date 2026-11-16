package com.pedidos360.cart.usecase;

import com.pedidos360.cart.cache.OrderResultRegistry;
import com.pedidos360.cart.messaging.dto.OrderResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterOrderResult {

    private static final Logger log = LoggerFactory.getLogger(RegisterOrderResult.class);
    private final OrderResultRegistry registry;

    public RegisterOrderResult(OrderResultRegistry registry) {
        this.registry = registry;
    }

    public void execute(OrderResultMessage message) {
        registry.put(message);
        log.info("Resultado de pedido cartId={} status={} motivo={}",
                message.cartId(), message.status(), message.reason());
    }
}