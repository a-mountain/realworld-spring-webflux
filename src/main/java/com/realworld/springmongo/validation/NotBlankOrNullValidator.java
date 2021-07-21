package com.realworld.springmongo.validation;

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotBlankOrNullValidator implements ConstraintValidator<NotBlankOrNull, String> {

    private final NotBlankValidator notBlankValidator = new NotBlankValidator();

    public boolean isValid(String obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }
        return notBlankValidator.isValid(obj, context);
    }
}
