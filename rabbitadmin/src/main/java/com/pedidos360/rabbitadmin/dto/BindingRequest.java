package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindingRequest(
        @NotBlank(message = "exchange es obligatorio") String exchange,
        @NotBlank(message = "queue es obligatorio") String queue,
        @NotNull(message = "routingKey es obligatorio (puede ir vacío solo en fanout)") String routingKey
) {}