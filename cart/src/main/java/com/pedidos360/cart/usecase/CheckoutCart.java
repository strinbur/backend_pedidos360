package com.pedidos360.cart.usecase;

import com.pedidos360.cart.dto.CartRequestDTO;
import com.pedidos360.cart.dto.CartResponseDTO;
import com.pedidos360.cart.messaging.dto.CheckoutItemMessage;
import com.pedidos360.cart.messaging.dto.CheckoutMessage;
import com.pedidos360.cart.messaging.producer.PublishCheckout;
import com.pedidos360.cart.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CheckoutCart {

    private final CartService cartService;
    private final PublishCheckout publishCheckout;

    public CheckoutCart(CartService cartService, PublishCheckout publishCheckout) {
        this.cartService = cartService;
        this.publishCheckout = publishCheckout;
    }

    public CartResponseDTO execute(CartRequestDTO request) {
        // 1. Valida productos y stock por REST (reutiliza tu lógica actual)
        CartResponseDTO calculated = cartService.calculate(request);

        // 2. Arma el mensaje
        List<CheckoutItemMessage> items = request.items().stream()
                .map(i -> new CheckoutItemMessage(i.productId(), i.quantity()))
                .toList();
        CheckoutMessage message = new CheckoutMessage(UUID.randomUUID().toString(), null, items);

        // 3. Publica; product descontará el stock de forma asíncrona
        publishCheckout.execute(message);

        return calculated;
    }
}