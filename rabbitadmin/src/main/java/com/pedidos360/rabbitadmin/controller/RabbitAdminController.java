package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.BindingRequest;
import com.pedidos360.rabbitadmin.dto.CreateExchangeRequest;
import com.pedidos360.rabbitadmin.dto.CreateQueueRequest;
import com.pedidos360.rabbitadmin.dto.MessageResponse;
import com.pedidos360.rabbitadmin.usecase.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RabbitAdminController {

    private static final String NAME_REGEX = "^[A-Za-z0-9._-]+$";

    private final CreateQueve createQueue;
    private final DeleteQueve deleteQueue;
    private final CreateExchange createExchange;
    private final DeleteExchange deleteExchange;
    private final CreateBinding createBinding;
    private final DeleteBinding deleteBinding;

    public RabbitAdminController(CreateQueve createQueue, DeleteQueve deleteQueue,
                                 CreateExchange createExchange, DeleteExchange deleteExchange,
                                 CreateBinding createBinding, DeleteBinding deleteBinding) {
        this.createQueue = createQueue;
        this.deleteQueue = deleteQueue;
        this.createExchange = createExchange;
        this.deleteExchange = deleteExchange;
        this.createBinding = createBinding;
        this.deleteBinding = deleteBinding;
    }

    // ---------- Colas ----------
    @PostMapping("/queues")
    public ResponseEntity<MessageResponse> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        createQueue.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Cola creada: " + request.name()));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable @Pattern(regexp = NAME_REGEX) String name) {
        deleteQueue.execute(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Exchanges ----------
    @PostMapping("/exchanges")
    public ResponseEntity<MessageResponse> createExchange(@Valid @RequestBody CreateExchangeRequest request) {
        createExchange.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Exchange creado: " + request.name()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable @Pattern(regexp = NAME_REGEX) String name) {
        deleteExchange.execute(name);
        return ResponseEntity.noContent().build();
    }

    // ---------- Bindings ----------
    @PostMapping("/bindings")
    public ResponseEntity<MessageResponse> createBinding(@Valid @RequestBody BindingRequest request) {
        createBinding.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Binding creado"));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest request) {
        deleteBinding.execute(request);
        return ResponseEntity.noContent().build();
    }
}