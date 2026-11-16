package com.pedidos360.cart.controller;

import com.pedidos360.cart.cache.OrderResultRegistry;
import com.pedidos360.cart.dto.CartRequestDTO;
import com.pedidos360.cart.dto.CartResponseDTO;
import com.pedidos360.cart.dto.CheckoutResponseDTO;
import com.pedidos360.cart.dto.OrderStatusDTO;
import com.pedidos360.cart.service.CartService;
import com.pedidos360.cart.usecase.CheckoutCart;
import com.pedidos360.cart.messaging.dto.OrderResultStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CheckoutCart checkoutCart;
    private final OrderResultRegistry orderResults;

    public CartController(CartService cartService, CheckoutCart checkoutCart,
                          OrderResultRegistry orderResults) {
        this.cartService = cartService;
        this.checkoutCart = checkoutCart;
        this.orderResults = orderResults;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CartResponseDTO> calculate(@Valid @RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.calculate(request));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(@Valid @RequestBody CartRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(checkoutCart.execute(request));
    }

    @GetMapping("/orders/{cartId}")
    public ResponseEntity<OrderStatusDTO> orderStatus(@PathVariable String cartId) {
        return ResponseEntity.ok(orderResults.find(cartId)
                .map(r -> new OrderStatusDTO(cartId,
                        r.status() == OrderResultStatus.CONFIRMED ? "CONFIRMADA" : "RECHAZADA", r.reason()))
                .orElse(new OrderStatusDTO(cartId, "PENDIENTE", null)));
    }
}