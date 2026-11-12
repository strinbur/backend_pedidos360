package com.pedidos360.kafkaadmin.exception;

public class TopicNotFoundException extends RuntimeException {
    public TopicNotFoundException(String message) { super(message); }
}