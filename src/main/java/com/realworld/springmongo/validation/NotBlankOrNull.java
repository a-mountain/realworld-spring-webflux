package com.realworld.springmongo.validation;

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankOrNullValidator.class)
public @interface NotBlankOrNull {

    String message() default "{javax.validation.constraints.NotBlank.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class NotBlankOrNullValidator implements ConstraintValidator<NotBlankOrNull, String> {

    private final NotBlankValidator notBlankValidator = new NotBlankValidator();

    public boolean isValid(String obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }
        return notBlankValidator.isValid(obj, context);
    }
}
