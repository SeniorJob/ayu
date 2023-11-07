package com.seniorjob.seniorjobserver.exception;

public class CustomJwtUnsupportedException extends RuntimeException {
    public CustomJwtUnsupportedException(String message) {
        super(message);
    }
}

