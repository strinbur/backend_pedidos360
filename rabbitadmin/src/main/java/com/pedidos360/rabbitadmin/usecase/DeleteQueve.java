package com.pedidos360.rabbitadmin.usecase;

import com.pedidos360.rabbitadmin.broker.BrokerAdminService;
import org.springframework.stereotype.Service;

@Service
public class DeleteQueve {

    private final BrokerAdminService broker;

    public DeleteQueve(BrokerAdminService broker) {
        this.broker = broker;
    }

    public void execute(String name) {
        broker.deleteQueue(name);
    }
}