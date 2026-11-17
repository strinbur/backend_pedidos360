package com.pedidos360.product.messaging.sse;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockAlertsSseConsumer {

    private final AlertsHub hub;

    public StockAlertsSseConsumer(AlertsHub hub) {
        this.hub = hub;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.stock-alerts}",
            groupId = "product-alerts-sse-#{T(java.util.UUID).randomUUID().toString()}",
            properties = {"auto.offset.reset=latest", "enable.auto.commit=false"})
    public void onAlert(String json) {
        hub.broadcast(json);
    }
}