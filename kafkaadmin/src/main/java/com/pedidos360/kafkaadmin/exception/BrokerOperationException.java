package com.pedidos360.kafkaadmin.exception;

public class BrokerOperationException extends RuntimeException {
    public BrokerOperationException(String message) { super(message); }
}