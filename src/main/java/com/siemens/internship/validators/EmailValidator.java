package com.siemens.internship.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * This is a custom class that checks if an email is in valid format or not, using a REGEX.
 * */
@Component
public class EmailValidator implements ConstraintValidator<ValidateEmail, String> {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Validates if the email address is valid or not.
     * @param email email address to be validated
     * @param constraintValidatorContext custom annotation context
     * @return true - email valid, false - invalid email
     * */
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return email != null && EMAIL_REGEX.matcher(email).matches();
    }
}
