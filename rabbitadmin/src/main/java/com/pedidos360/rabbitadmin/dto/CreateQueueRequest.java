package com.pedidos360.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateQueueRequest(
        @NotBlank(message = "name es obligatorio")
        @Size(max = 255, message = "name admite máximo 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "name solo admite letras, números, punto, guion y guion bajo")
        String name,

        String deadLetterExchange,
        String deadLetterRoutingKey
) {}