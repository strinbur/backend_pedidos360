package com.pedidos360.cart.messaging.dto;

public record OrderPlacedItem(String productId, String name, int quantity, double subtotal) {}