package com.nourish1709.pool.connection.exception;

public class MethodNotImplementedException extends UnsupportedOperationException {
    public MethodNotImplementedException() {
        this("Method is not implemented");
    }

    public MethodNotImplementedException(String message) {
        super(message);
    }
}
