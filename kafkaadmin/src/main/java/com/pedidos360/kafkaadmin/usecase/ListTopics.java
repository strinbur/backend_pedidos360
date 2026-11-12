package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.broker.BrokerAdminService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListTopics {

    private final BrokerAdminService broker;

    public ListTopics(BrokerAdminService broker) {
        this.broker = broker;
    }

    public List<String> execute() {
        return broker.listTopics();
    }
}