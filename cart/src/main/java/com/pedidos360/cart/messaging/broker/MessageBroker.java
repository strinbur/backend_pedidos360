package com.pedidos360.cart.messaging.broker;

public interface MessageBroker {
    void publish(String exchange, String routingKey, Object message);
}