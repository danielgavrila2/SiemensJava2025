package com.siemens.internship.model;

import com.siemens.internship.validators.ValidateEmail;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import lombok.*;

/**
 * This is the main entity of our application.
 * Item class is mapped to a database using Spring Boot JPA annotation.
 * */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;

    /*
     - We could use @Email annotation provided by Spring Boot as a validation method.
     - This method might be better than providing a custom implementation for an email address, especially in enterprise apps.
     */
    @ValidateEmail(message = "Email address has to be valid!")
    private String email;
}