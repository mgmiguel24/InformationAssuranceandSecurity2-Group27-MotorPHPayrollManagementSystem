package com.motorph.service;

/** Wrong credentials or missing session. Controller maps this to HTTP 401. */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}
