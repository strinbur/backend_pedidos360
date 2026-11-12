package com.pedidos360.kafkaadmin.exception;

public class TopicAlreadyExistsException extends RuntimeException {
    public TopicAlreadyExistsException(String message) { super(message); }
}