package com.pedidos360.kafkaadmin.usecase;

import com.pedidos360.kafkaadmin.config.AdminProperties;
import com.pedidos360.kafkaadmin.exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class TopicGuard {

    private final AdminProperties props;

    public TopicGuard(AdminProperties props) {
        this.props = props;
    }

    public void assertNotProtected(String name) {
        if (name.startsWith(props.protectedTopicPrefix())) {
            throw new InvalidRequestException(
                    "Los tópicos que empiezan con '" + props.protectedTopicPrefix() + "' son internos de Kafka y no se pueden modificar");
        }
    }
}