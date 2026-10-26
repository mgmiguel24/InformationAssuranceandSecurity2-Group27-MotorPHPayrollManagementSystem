package com.motorph.service;

/** Logged in but not allowed. Controller maps this to HTTP 403. */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}
