package ru.story1click.jwt.service;


public interface JwtService {

    boolean validate(String jwt, String originalUri);
}
