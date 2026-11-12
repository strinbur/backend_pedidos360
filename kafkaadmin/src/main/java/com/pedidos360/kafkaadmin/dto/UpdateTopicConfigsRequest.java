package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record UpdateTopicConfigsRequest(
        @NotEmpty(message = "configs no puede estar vacío")
        Map<String, String> configs
) {}