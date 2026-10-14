package com.pedidos360.product.messaging.broker;

public interface MessageBroker {
    void publish(String exchange, String routingKey, Object message);
}