package com.pedidos360.cart.cache;

import com.pedidos360.cart.messaging.dto.OrderResultMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OrderResultRegistry {

    private static final int MAX_ENTRIES = 5000;

    // Acotado: descarta los resultados más antiguos para no crecer sin límite
    private final Map<String, OrderResultMessage> results = Collections.synchronizedMap(
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, OrderResultMessage> eldest) {
                    return size() > MAX_ENTRIES;
                }
            });

    public void put(OrderResultMessage message) {
        results.put(message.cartId(), message);
    }

    public Optional<OrderResultMessage> find(String cartId) {
        return Optional.ofNullable(results.get(cartId));
    }
}