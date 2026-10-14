package com.pedidos360.product.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbit")
public record RabbitProperties(Exchanges exchanges, Queues queues, RoutingKeys routingKeys) {

    public record Exchanges(String direct, String topic, String dlx) {}

    public record Queues(String stock, String stockDlq,
                         String productEvents, String productEventsDlq) {}

    public record RoutingKeys(String stockReduce, String productUpdated,
                              String productDeleted, String productEventsBinding) {}
}