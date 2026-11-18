package com.pedidos360.product.messaging.streams;

import com.pedidos360.product.messaging.dto.OrderResultMessage;
import com.pedidos360.product.messaging.dto.OrderResultStatus;
import com.pedidos360.product.messaging.dto.SalesTotal;
import com.pedidos360.product.messaging.dto.SalesVelocity;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableKafkaStreams
public class SalesStreamsConfig {

    private static final Logger log = LoggerFactory.getLogger(SalesStreamsConfig.class);

    public static final String TOTALS_STORE = "sales-totals-store";
    public static final String VELOCITY_STORE = "sales-velocity-store";

    @Bean
    public KStream<String, String> salesTotalsStream(StreamsBuilder builder, JsonMapper mapper,
            @Value("${app.kafka.topics.orders-result}") String ordersResultTopic,
            @Value("${app.kafka.topics.sales-totals}") String salesTotalsTopic,
            @Value("${app.kafka.topics.sales-velocity}") String salesVelocityTopic,
            @Value("${app.kafka.velocity-window-minutes:5}") int windowMinutes) {

        KStream<String, String> results = builder.stream(
                ordersResultTopic, Consumed.with(Serdes.String(), Serdes.String()));

        // Un pedido CONFIRMED -> un registro (productId, SalesTotal) por ítem
        KStream<String, String> items = results.flatMap((cartId, json) -> toSoldItems(mapper, json));

        // 1) Totales acumulados por producto
        items
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .aggregate(
                        () -> "",
                        (productId, itemJson, current) -> accumulate(mapper, current, itemJson),
                        Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(TOTALS_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.String()))
                .toStream()
                .to(salesTotalsTopic, Produced.with(Serdes.String(), Serdes.String()));

        // 2) Velocidad de venta: unidades por producto en ventanas de N minutos
        KStream<String, Long> units = items.mapValues(
                itemJson -> mapper.readValue(itemJson, SalesTotal.class).units());

        units
                .groupByKey(Grouped.with("sales-velocity", Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(windowMinutes)))
                .reduce(Long::sum,
                        Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(VELOCITY_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()))
                .toStream()
                .map((windowedKey, total) -> KeyValue.pair(
                        windowedKey.key(),
                        mapper.writeValueAsString(new SalesVelocity(
                                windowedKey.key(), total,
                                windowedKey.window().startTime().toString(),
                                windowedKey.window().endTime().toString()))))
                .to(salesVelocityTopic, Produced.with(Serdes.String(), Serdes.String()));

        return results;
    }

    private List<KeyValue<String, String>> toSoldItems(JsonMapper mapper, String json) {
        try {
            OrderResultMessage msg = mapper.readValue(json, OrderResultMessage.class);
            if (msg.status() != OrderResultStatus.CONFIRMED) {
                return List.of();
            }
            return msg.items().stream()
                    .map(i -> KeyValue.pair(i.productId(),
                            mapper.writeValueAsString(new SalesTotal(i.productId(), i.name(), i.quantity()))))
                    .toList();
        } catch (JacksonException e) {
            log.error("orders.result ilegible en Streams, se ignora: {}", e.getMessage());
            return List.of();
        }
    }

    private String accumulate(JsonMapper mapper, String current, String itemJson) {
        SalesTotal item = mapper.readValue(itemJson, SalesTotal.class);
        if (current == null || current.isEmpty()) {
            return itemJson;
        }
        SalesTotal total = mapper.readValue(current, SalesTotal.class);
        String name = item.name() != null ? item.name() : total.name();
        return mapper.writeValueAsString(new SalesTotal(item.productId(), name, total.units() + item.units()));
    }
}