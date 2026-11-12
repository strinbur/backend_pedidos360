package com.pedidos360.kafkaadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String protectedTopicPrefix) {}