package com.pedidos360.product.messaging.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertsHub {

    private static final Logger log = LoggerFactory.getLogger(AlertsHub.class);

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);          // sin timeout propio
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
        log.info("Cliente SSE conectado, activos={}", emitters.size());
        return emitter;
    }

    public void broadcast(String json) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("stock-alert").data(json, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }

    /** Mantiene viva la conexión (proxies) y limpia clientes caídos. */
    @Scheduled(fixedRate = 20_000)
    public void heartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("keep-alive"));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }
}