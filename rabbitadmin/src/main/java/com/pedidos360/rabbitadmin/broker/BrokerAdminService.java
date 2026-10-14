package com.pedidos360.rabbitadmin.broker;

public interface BrokerAdminService {

    void createQueue(String name, String deadLetterExchange, String deadLetterRoutingKey);

    void deleteQueue(String name);

    void createExchange(String name, String type);

    void deleteExchange(String name);

    void createBinding(String exchange, String queue, String routingKey);

    void deleteBinding(String exchange, String queue, String routingKey);
}