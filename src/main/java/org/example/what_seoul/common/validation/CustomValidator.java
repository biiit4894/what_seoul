package org.example.what_seoul.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CustomValidator {
    private final Validator validator;

    public <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }
}
