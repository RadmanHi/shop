package com.radman.shop.common.annotation.validator;

import com.radman.shop.common.annotation.InRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.util.Objects;

@Component
public class InRangeValidator implements ConstraintValidator<InRange, Integer> {

    private final Environment env;
    private int min;
    private int max;

    public InRangeValidator(Environment env) {
        this.env = env;
    }

    @Override
    public void initialize(InRange a) {
        this.min = Integer.parseInt(Objects.requireNonNull(env.getProperty(a.min()), a.min()));
        this.max = Integer.parseInt(Objects.requireNonNull(env.getProperty(a.max()), a.max()));

        if (min > max) throw new InvalidParameterException("Invalid InRange config");
    }

    @Override
    public boolean isValid(Integer v, ConstraintValidatorContext c) {
        return v == null || (v >= min && v <= max);
    }
}