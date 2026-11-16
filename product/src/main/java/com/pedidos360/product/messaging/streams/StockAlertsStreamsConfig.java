package com.pedidos360.product.messaging.streams;

import com.pedidos360.product.messaging.dto.StockAlert;
import com.pedidos360.product.messaging.dto.StockLowMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class StockAlertsStreamsConfig {

    private static final Logger log = LoggerFactory.getLogger(StockAlertsStreamsConfig.class);

    public static final String LEVEL_STORE = "stock-level-store";

    @Bean
    public KStream<String, String> stockAlertsStream(StreamsBuilder builder, JsonMapper mapper,
            @Value("${app.kafka.topics.stock-low}") String stockLowTopic,
            @Value("${app.kafka.topics.stock-alerts}") String stockAlertsTopic) {

        KStream<String, String> events = builder.stream(
                stockLowTopic, Consumed.with(Serdes.String(), Serdes.String()));

        // Estado por producto: último nivel conocido + si este evento cambió el nivel
        events
                .groupByKey(Grouped.with("stock-level", Serdes.String(), Serdes.String()))
                .aggregate(
                        () -> "",
                        (productId, eventJson, stateJson) -> nextState(mapper, stateJson, eventJson),
                        Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(LEVEL_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String())
                                .withCachingDisabled())      // cada evento emite: no se pierden transiciones
                .toStream()
                .filter((productId, stateJson) -> isChanged(mapper, stateJson))
                .to(stockAlertsTopic, Produced.with(Serdes.String(), Serdes.String()));

        return events;
    }

    private String nextState(JsonMapper mapper, String stateJson, String eventJson) {
        try {
            StockLowMessage e = mapper.readValue(eventJson, StockLowMessage.class);
            if (e.level() == null) {                       // evento antiguo, sin nivel
                return markUnchanged(mapper, stateJson);
            }
            boolean changed = true;
            if (stateJson != null && !stateJson.isEmpty()) {
                StockAlert prev = mapper.readValue(stateJson, StockAlert.class);
                changed = !e.level().equals(prev.level());
            }
            return mapper.writeValueAsString(new StockAlert(
                    e.productId(), e.code(), e.name(), e.stock(), e.threshold(), e.level(), changed));
        } catch (RuntimeException ex) {
            log.error("stock.low ilegible en Streams, se ignora: {}", ex.getMessage());
            return markUnchanged(mapper, stateJson);
        }
    }

    private String markUnchanged(JsonMapper mapper, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) {
            return "";
        }
        StockAlert prev = mapper.readValue(stateJson, StockAlert.class);
        return mapper.writeValueAsString(new StockAlert(
                prev.productId(), prev.code(), prev.name(), prev.stock(), prev.threshold(), prev.level(), false));
    }

    private boolean isChanged(JsonMapper mapper, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) {
            return false;
        }
        try {
            return mapper.readValue(stateJson, StockAlert.class).changed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}