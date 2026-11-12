package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateTopicRequest(
        @NotBlank(message = "name es obligatorio")
        @Size(max = 249, message = "name admite máximo 249 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "name solo admite letras, números, punto, guion y guion bajo")
        String name,

        @Min(value = 1, message = "partitions debe ser al menos 1")
        int partitions,

        @Min(value = 1, message = "replicationFactor debe ser al menos 1")
        short replicationFactor,

        Map<String, String> configs
) {}