package com.pedidos360.rabbitadmin.usecase;

import com.pedidos360.rabbitadmin.broker.BrokerAdminService;
import com.pedidos360.rabbitadmin.config.AdminProperties;
import com.pedidos360.rabbitadmin.exception.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class DeleteExchange {

    private final BrokerAdminService broker;
    private final AdminProperties props;

    public DeleteExchange(BrokerAdminService broker, AdminProperties props) {
        this.broker = broker;
        this.props = props;
    }

    public void execute(String name) {
        if (name.startsWith(props.protectedExchangePrefix())) {
            throw new InvalidRequestException("No se pueden eliminar los exchanges reservados de RabbitMQ");
        }
        broker.deleteExchange(name);
    }
}