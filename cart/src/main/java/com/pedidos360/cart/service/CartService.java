package com.pedidos360.cart.service;

import com.pedidos360.cart.dto.CartRequestDTO;
import com.pedidos360.cart.dto.CartResponseDTO;

public interface CartService {

    CartResponseDTO calculate(CartRequestDTO request);
}