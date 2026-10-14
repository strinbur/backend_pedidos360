package com.pedidos360.rabbitadmin.usecase;

import com.pedidos360.rabbitadmin.broker.BrokerAdminService;
import com.pedidos360.rabbitadmin.config.AdminProperties;
import com.pedidos360.rabbitadmin.dto.CreateExchangeRequest;
import com.pedidos360.rabbitadmin.exception.InvalidRequestException;
import org.springframework.stereotype.Service;

@Service
public class CreateExchange {

    private final BrokerAdminService broker;
    private final AdminProperties props;

    public CreateExchange(BrokerAdminService broker, AdminProperties props) {
        this.broker = broker;
        this.props = props;
    }

    public void execute(CreateExchangeRequest request) {
        if (request.name().startsWith(props.protectedExchangePrefix())) {
            throw new InvalidRequestException(
                    "Los nombres que empiezan con '" + props.protectedExchangePrefix() + "' están reservados por RabbitMQ");
        }
        broker.createExchange(request.name(), request.type());
    }
}