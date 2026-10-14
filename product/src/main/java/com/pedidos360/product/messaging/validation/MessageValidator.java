package com.pedidos360.product.messaging.validation;

import com.pedidos360.product.messaging.exception.InvalidMessageException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MessageValidator {

    private final Validator validator;

    public MessageValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T message) {
        Set<ConstraintViolation<T>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            String detail = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new InvalidMessageException(detail);
        }
    }
}