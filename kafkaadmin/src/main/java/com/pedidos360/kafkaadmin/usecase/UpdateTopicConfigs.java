package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import com.pedidos360.kafkaadmin.dto.UpdateTopicConfigsRequest;
import org.springframework.stereotype.Service;

@Service
public class UpdateTopicConfigs {

    private final BrokerAdminService broker;
    private final TopicGuard guard;

    public UpdateTopicConfigs(BrokerAdminService broker, TopicGuard guard) {
        this.broker = broker;
        this.guard = guard;
    }

    public void execute(String name, UpdateTopicConfigsRequest request) {
        guard.assertNotProtected(name);
        broker.setConfigs(name, request.configs());
    }
}