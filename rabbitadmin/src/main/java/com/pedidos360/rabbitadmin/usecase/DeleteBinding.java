package com.pedidos360.rabbitadmin.usecase;

import com.pedidos360.rabbitadmin.broker.BrokerAdminService;
import com.pedidos360.rabbitadmin.dto.BindingRequest;
import org.springframework.stereotype.Service;

@Service
public class DeleteBinding {

    private final BrokerAdminService broker;

    public DeleteBinding(BrokerAdminService broker) {
        this.broker = broker;
    }

    public void execute(BindingRequest request) {
        broker.deleteBinding(request.exchange(), request.queue(), request.routingKey());
    }
}