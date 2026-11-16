package com.pedidos360.cart.dto;

import java.util.List;

public record CheckoutResponseDTO(String cartId, String status,
                                  List<CartItemResponseDTO> items, double total) {}