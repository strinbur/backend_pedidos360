package com.pedidos360.kafkaadmin.dto;

import jakarta.validation.constraints.Min;

public record IncreasePartitionsRequest(
        @Min(value = 2, message = "partitions debe ser mayor al número actual de particiones")
        int partitions
) {}