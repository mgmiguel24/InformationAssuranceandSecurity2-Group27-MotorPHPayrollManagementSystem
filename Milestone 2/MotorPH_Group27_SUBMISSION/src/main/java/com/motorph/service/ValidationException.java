package com.motorph.service;

/** Bad input from the user. Controller maps this to HTTP 400. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
