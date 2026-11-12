package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import com.pedidos360.kafkaadmin.dto.IncreasePartitionsRequest;
import org.springframework.stereotype.Service;

@Service
public class IncreasePartitions {

    private final BrokerAdminService broker;
    private final TopicGuard guard;

    public IncreasePartitions(BrokerAdminService broker, TopicGuard guard) {
        this.broker = broker;
        this.guard = guard;
    }

    public void execute(String name, IncreasePartitionsRequest request) {
        guard.assertNotProtected(name);
        broker.increasePartitions(name, request.partitions());
    }
}