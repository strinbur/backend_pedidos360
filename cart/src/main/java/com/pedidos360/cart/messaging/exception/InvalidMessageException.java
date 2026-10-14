package com.pedidos360.cart.messaging.exception;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String detail) {
        super("Mensaje inválido: " + detail);
    }
}