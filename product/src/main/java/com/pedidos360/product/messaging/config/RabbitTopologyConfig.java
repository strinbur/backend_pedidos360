package com.pedidos360.product.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    private final RabbitProperties props;

    public RabbitTopologyConfig(RabbitProperties props) {
        this.props = props;
    }

    // ---------- Exchanges ----------
    @Bean DirectExchange directExchange() { return new DirectExchange(props.exchanges().direct(), true, false); }
    @Bean TopicExchange topicExchange()   { return new TopicExchange(props.exchanges().topic(), true, false); }
    @Bean DirectExchange dlxExchange()    { return new DirectExchange(props.exchanges().dlx(), true, false); }

    // ---------- Cola 1: stock (direct) ----------
    @Bean Queue stockQueue() {
        return QueueBuilder.durable(props.queues().stock())
                .quorum()
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(props.queues().stockDlq())
                .build();
    }
    @Bean Queue stockDlq() {
        return QueueBuilder.durable(props.queues().stockDlq()).quorum().build();
    }
    @Bean Binding stockBinding() {
        return BindingBuilder.bind(stockQueue()).to(directExchange())
                .with(props.routingKeys().stockReduce());
    }
    @Bean Binding stockDlqBinding() {
        return BindingBuilder.bind(stockDlq()).to(dlxExchange())
                .with(props.queues().stockDlq());
    }

    // ---------- Cola 2: eventos de producto (topic) ----------
    @Bean Queue productEventsQueue() {
        return QueueBuilder.durable(props.queues().productEvents())
                .quorum()
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(props.queues().productEventsDlq())
                .build();
    }
    @Bean Queue productEventsDlq() {
        return QueueBuilder.durable(props.queues().productEventsDlq()).quorum().build();
    }
    @Bean Binding productEventsBinding() {
        return BindingBuilder.bind(productEventsQueue()).to(topicExchange())
                .with(props.routingKeys().productEventsBinding());
    }
    @Bean Binding productEventsDlqBinding() {
        return BindingBuilder.bind(productEventsDlq()).to(dlxExchange())
                .with(props.queues().productEventsDlq());
    }

    // ---------- Conversor JSON ----------
    @Bean MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}