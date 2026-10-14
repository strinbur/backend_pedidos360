package com.pedidos360.cart.controller;

import com.pedidos360.cart.dto.CartRequestDTO;
import com.pedidos360.cart.dto.CartResponseDTO;
import com.pedidos360.cart.service.CartService;
import com.pedidos360.cart.usecase.CheckoutCart;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

    public CartController(CartService cartService, CheckoutCart checkoutCart) {
        this.cartService = cartService;
        this.checkoutCart = checkoutCart;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CartResponseDTO> calculate(@Valid @RequestBody CartRequestDTO request) {
        return ResponseEntity.ok(cartService.calculate(request));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDTO> checkout(@Valid @RequestBody CartRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(checkoutCart.execute(request));
    }
}