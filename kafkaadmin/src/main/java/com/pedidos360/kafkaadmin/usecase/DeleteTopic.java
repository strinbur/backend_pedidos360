package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import org.springframework.stereotype.Service;

@Service
public class DeleteTopic {

    private final BrokerAdminService broker;
    private final TopicGuard guard;

    public DeleteTopic(BrokerAdminService broker, TopicGuard guard) {
        this.broker = broker;
        this.guard = guard;
    }

    public void execute(String name) {
        guard.assertNotProtected(name);
        broker.deleteTopic(name);
    }
}