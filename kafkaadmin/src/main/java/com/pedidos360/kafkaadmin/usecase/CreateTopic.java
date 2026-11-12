package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import com.pedidos360.kafkaadmin.dto.CreateTopicRequest;
import org.springframework.stereotype.Service;

@Service
public class CreateTopic {

    private final BrokerAdminService broker;
    private final TopicGuard guard;

    public CreateTopic(BrokerAdminService broker, TopicGuard guard) {
        this.broker = broker;
        this.guard = guard;
    }

    public void execute(CreateTopicRequest request) {
        guard.assertNotProtected(request.name());
        broker.createTopic(request.name(), request.partitions(), request.replicationFactor(), request.configs());
    }
}