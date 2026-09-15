package com.pedidos360.cart.service.impl;

import com.pedidos360.cart.client.ProductClient;
import com.pedidos360.cart.dto.*;
import com.pedidos360.cart.exception.InsufficientStockException;
import com.pedidos360.cart.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final ProductClient productClient;

    public CartServiceImpl(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Override
    public CartResponseDTO calculate(CartRequestDTO request) {
        List<CartItemResponseDTO> items = request.items().stream()
                .map(this::buildCartItem)
                .toList();

        double total = items.stream()
                .mapToDouble(CartItemResponseDTO::subtotal)
                .sum();

        return new CartResponseDTO(items, total);
    }

    private CartItemResponseDTO buildCartItem(CartItemRequestDTO itemRequest) {
        ProductDTO product = productClient.getProductById(itemRequest.productId());

        if (itemRequest.quantity() > product.stock()) {
            throw new InsufficientStockException(product.name(), itemRequest.quantity(), product.stock());
        }

        double subtotal = product.price() * itemRequest.quantity();

        return new CartItemResponseDTO(
                product.id(),
                product.name(),
                product.price(),
                itemRequest.quantity(),
                subtotal
        );
    }
}