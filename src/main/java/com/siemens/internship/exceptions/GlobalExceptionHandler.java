package com.siemens.internship.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This class is meant to make clearly the exception handling and handles all custom exceptions thrown.
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This method handles the InvalidEmailException.
     * */
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<String> handleInvalidEmailException(InvalidEmailException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
