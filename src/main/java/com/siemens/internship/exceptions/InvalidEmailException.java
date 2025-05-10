package com.siemens.internship.exceptions;

/**
 * This is a custom exception which is thrown if an email is not valid.
 * **/
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String email) {
        super("Invalid email address : " + email);
    }
}
