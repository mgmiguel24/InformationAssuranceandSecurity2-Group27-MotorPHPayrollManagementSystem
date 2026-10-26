package com.motorph.dao;

/**
 * Thrown by every DAO when the database fails.
 * The raw SQLException is kept as the cause for server logs only;
 * the controller turns this into a generic HTTP 500 so no SQL detail leaks to users.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
