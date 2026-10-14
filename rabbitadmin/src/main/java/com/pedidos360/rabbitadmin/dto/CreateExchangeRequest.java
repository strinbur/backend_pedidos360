package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateExchangeRequest(
        @NotBlank(message = "name es obligatorio")
        @Size(max = 255, message = "name admite máximo 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "name solo admite letras, números, punto, guion y guion bajo")
        String name,

        @NotBlank(message = "type es obligatorio")
        @Pattern(regexp = "direct|topic|fanout|headers", message = "type debe ser direct, topic, fanout o headers")
        String type
) {}