package com.pedidos360.product.messaging.exception;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String detail) {
        super("Mensaje inválido: " + detail);
    }
}