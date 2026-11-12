package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import com.pedidos360.kafkaadmin.dto.TopicResponse;
import org.springframework.stereotype.Service;

@Service
public class DescribeTopic {

    private final BrokerAdminService broker;

    public DescribeTopic(BrokerAdminService broker) {
        this.broker = broker;
    }

    public TopicResponse execute(String name) {
        return broker.describeTopic(name);
    }
}