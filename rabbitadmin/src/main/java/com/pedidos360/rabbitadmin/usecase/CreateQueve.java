package com.pedidos360.rabbitadmin.usecase;

import com.pedidos360.rabbitadmin.broker.BrokerAdminService;
import com.pedidos360.rabbitadmin.dto.CreateQueueRequest;
import org.springframework.stereotype.Service;

@Service
public class CreateQueve {

    private final BrokerAdminService broker;

    public CreateQueve(BrokerAdminService broker) {
        this.broker = broker;
    }

    public void execute(CreateQueueRequest request) {
        broker.createQueue(request.name(), request.deadLetterExchange(), request.deadLetterRoutingKey());
    }
}