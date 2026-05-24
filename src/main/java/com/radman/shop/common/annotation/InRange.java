package com.radman.shop.common.annotation;

import com.radman.shop.common.annotation.validator.InRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = InRangeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InRange {

    String message() default "value out of range";

    String min();

    String max();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}