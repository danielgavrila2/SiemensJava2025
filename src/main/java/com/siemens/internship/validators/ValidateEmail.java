package com.siemens.internship.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom functional interface which will handle the Email validation using a REGEX-based mechanism,
 * provided by <b>EmailValidator</b> class.
 * */

@Documented
@Constraint(validatedBy = { EmailValidator.class })
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateEmail {

    String message() default "The email address is invalid!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
