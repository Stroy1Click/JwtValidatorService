package ru.story1click.jwt.service;


public interface JwtValidationService {

    boolean validate(String jwt, String originalUri);
}
