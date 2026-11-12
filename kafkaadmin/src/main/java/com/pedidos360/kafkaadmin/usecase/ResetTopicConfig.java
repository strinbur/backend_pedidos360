package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import org.springframework.stereotype.Service;

@Service
public class ResetTopicConfig {

    private final BrokerAdminService broker;
    private final TopicGuard guard;

    public ResetTopicConfig(BrokerAdminService broker, TopicGuard guard) {
        this.broker = broker;
        this.guard = guard;
    }

    public void execute(String name, String key) {
        guard.assertNotProtected(name);
        broker.resetConfig(name, key);
    }
}