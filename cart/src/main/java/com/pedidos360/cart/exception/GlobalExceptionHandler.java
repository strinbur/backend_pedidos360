package com.pedidos360.cart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ProductNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.client.ResourceAccessException.class)
    public ResponseEntity<Object> handleProductDown(org.springframework.web.client.ResourceAccessException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de productos no está disponible");
    }

    @ExceptionHandler(org.springframework.amqp.AmqpException.class)
    public ResponseEntity<Object> handleBrokerDown(org.springframework.amqp.AmqpException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo enviar el pedido, intenta nuevamente");
    }

    private ResponseEntity<Object> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}