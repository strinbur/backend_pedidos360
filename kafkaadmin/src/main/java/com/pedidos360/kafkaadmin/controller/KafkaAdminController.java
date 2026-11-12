package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.dto.*;
import com.pedidos360.kafkaadmin.usecase.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topics")
public class KafkaAdminController {

    private static final String NAME_REGEX = "^[A-Za-z0-9._-]+$";

    private final CreateTopic createTopic;
    private final DeleteTopic deleteTopic;
    private final ListTopics listTopics;
    private final DescribeTopic describeTopic;
    private final UpdateTopicConfigs updateTopicConfigs;
    private final ResetTopicConfig resetTopicConfig;
    private final IncreasePartitions increasePartitions;

    public KafkaAdminController(CreateTopic createTopic, DeleteTopic deleteTopic,
                                ListTopics listTopics, DescribeTopic describeTopic,
                                UpdateTopicConfigs updateTopicConfigs, ResetTopicConfig resetTopicConfig,
                                IncreasePartitions increasePartitions) {
        this.createTopic = createTopic;
        this.deleteTopic = deleteTopic;
        this.listTopics = listTopics;
        this.describeTopic = describeTopic;
        this.updateTopicConfigs = updateTopicConfigs;
        this.resetTopicConfig = resetTopicConfig;
        this.increasePartitions = increasePartitions;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateTopicRequest request) {
        createTopic.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Tópico creado: " + request.name()));
    }

    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(listTopics.execute());
    }

    @GetMapping("/{name}")
    public ResponseEntity<TopicResponse> describe(@PathVariable @Pattern(regexp = NAME_REGEX) String name) {
        return ResponseEntity.ok(describeTopic.execute(name));
    }

    @PutMapping("/{name}/configs")
    public ResponseEntity<MessageResponse> updateConfigs(@PathVariable @Pattern(regexp = NAME_REGEX) String name,
                                                         @Valid @RequestBody UpdateTopicConfigsRequest request) {
        updateTopicConfigs.execute(name, request);
        return ResponseEntity.ok(new MessageResponse("Configuración actualizada: " + name));
    }

    @DeleteMapping("/{name}/configs/{key}")
    public ResponseEntity<Void> resetConfig(@PathVariable @Pattern(regexp = NAME_REGEX) String name,
                                            @PathVariable @Pattern(regexp = NAME_REGEX) String key) {
        resetTopicConfig.execute(name, key);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{name}/partitions")
    public ResponseEntity<MessageResponse> partitions(@PathVariable @Pattern(regexp = NAME_REGEX) String name,
                                                      @Valid @RequestBody IncreasePartitionsRequest request) {
        increasePartitions.execute(name, request);
        return ResponseEntity.ok(new MessageResponse("Particiones aumentadas: " + name));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(@PathVariable @Pattern(regexp = NAME_REGEX) String name) {
        deleteTopic.execute(name);
        return ResponseEntity.noContent().build();
    }
}