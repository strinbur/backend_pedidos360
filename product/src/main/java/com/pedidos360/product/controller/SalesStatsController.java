package com.pedidos360.product.controller;

import com.pedidos360.product.messaging.dto.SalesTotal;
import com.pedidos360.product.messaging.dto.StockAlert;
import com.pedidos360.product.messaging.streams.SalesStreamsConfig;
import com.pedidos360.product.messaging.streams.StockAlertsStreamsConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import com.pedidos360.product.messaging.sse.AlertsHub;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/products/stats")
public class SalesStatsController {

    private final StreamsBuilderFactoryBean streamsFactory;
    private final JsonMapper mapper;

    private final AlertsHub alertsHub;

    public SalesStatsController(StreamsBuilderFactoryBean streamsFactory, JsonMapper mapper, AlertsHub alertsHub) {
        this.streamsFactory = streamsFactory;
        this.mapper = mapper;
        this.alertsHub = alertsHub;
    }

    @GetMapping("/top-sellers")
    public List<SalesTotal> topSellers(@RequestParam(defaultValue = "5") int limit) {
        List<SalesTotal> all = new ArrayList<>();
        try (KeyValueIterator<String, String> it = store(SalesStreamsConfig.TOTALS_STORE).all()) {
            while (it.hasNext()) {
                all.add(mapper.readValue(it.next().value, SalesTotal.class));
            }
        }
        return all.stream()
                .sorted(Comparator.comparingLong(SalesTotal::units).reversed())
                .limit(Math.max(1, Math.min(limit, 50)))
                .toList();
    }

    /** Productos que están en LOW u OUT en este momento. */
    @GetMapping("/stock-alerts")
    public List<StockAlert> stockAlerts() {
        List<StockAlert> current = new ArrayList<>();
        try (KeyValueIterator<String, String> it = store(StockAlertsStreamsConfig.LEVEL_STORE).all()) {
            while (it.hasNext()) {
                String json = it.next().value;
                if (json == null || json.isEmpty()) {
                    continue;
                }
                StockAlert alert = mapper.readValue(json, StockAlert.class);
                if (!"OK".equals(alert.level())) {
                    current.add(alert);
                }
            }
        }
        return current.stream().sorted(Comparator.comparingInt(StockAlert::stock)).toList();
    }

    private ReadOnlyKeyValueStore<String, String> store(String name) {
        KafkaStreams streams = streamsFactory.getKafkaStreams();
        if (streams == null || streams.state() != KafkaStreams.State.RUNNING) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Las estadísticas aún se están cargando, intenta en unos segundos");
        }
        return streams.store(StoreQueryParameters.fromNameAndType(name, QueryableStoreTypes.keyValueStore()));
    }


    @GetMapping(value = "/alerts/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter alertsStream() {
        return alertsHub.subscribe();
    }
}