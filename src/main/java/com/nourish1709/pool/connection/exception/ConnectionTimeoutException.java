package com.nourish1709.pool.connection.exception;

public class ConnectionTimeoutException extends RuntimeException {
    public ConnectionTimeoutException(String message) {
        super(message);
    }
}
