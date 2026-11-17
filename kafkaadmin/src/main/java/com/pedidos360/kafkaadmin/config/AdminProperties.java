package com.pedidos360.kafkaadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String protectedTopicPrefix, List<String> protectedTopics) {}