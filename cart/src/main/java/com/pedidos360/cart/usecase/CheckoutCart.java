package com.pedidos360.cart.usecase;

import com.pedidos360.cart.dto.CartRequestDTO;
import com.pedidos360.cart.dto.CartResponseDTO;
import com.pedidos360.cart.dto.CheckoutResponseDTO;
import com.pedidos360.cart.messaging.dto.CheckoutItemMessage;
import com.pedidos360.cart.messaging.dto.CheckoutMessage;
import com.pedidos360.cart.messaging.dto.OrderPlacedItem;
import com.pedidos360.cart.messaging.dto.OrderPlacedMessage;
import com.pedidos360.cart.messaging.producer.PublishCheckout;
import com.pedidos360.cart.messaging.producer.PublishOrderPlaced;
import com.pedidos360.cart.service.CartService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutCart {

    private final CartService cartService;
    private final PublishCheckout publishCheckout;
    private final PublishOrderPlaced publishOrderPlaced;

    public CheckoutCart(CartService cartService, PublishCheckout publishCheckout,
                        PublishOrderPlaced publishOrderPlaced) {
        this.cartService = cartService;
        this.publishCheckout = publishCheckout;
        this.publishOrderPlaced = publishOrderPlaced;
    }

    public CheckoutResponseDTO execute(CartRequestDTO request) {
        CartResponseDTO calculated = cartService.calculate(request);

        String cartId = UUID.randomUUID().toString();
        List<CheckoutItemMessage> items = request.items().stream()
                .map(i -> new CheckoutItemMessage(i.productId(), i.quantity()))
                .toList();
        publishCheckout.execute(new CheckoutMessage(cartId, null, items));
        publishOrderPlaced.execute(toOrderPlaced(cartId, calculated));

        return new CheckoutResponseDTO(cartId, "PENDIENTE", calculated.items(), calculated.total());
    }

    private OrderPlacedMessage toOrderPlaced(String cartId, CartResponseDTO calculated) {
        List<OrderPlacedItem> items = calculated.items().stream()
                .map(i -> new OrderPlacedItem(i.productId(), i.name(), i.quantity(), i.subtotal()))
                .toList();
        return new OrderPlacedMessage(cartId, calculated.total(), Instant.now().toString(), items);
    }
}