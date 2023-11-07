package com.seniorjob.seniorjobserver.exception;

public class CustomJwtExpiredException extends RuntimeException {
    public CustomJwtExpiredException(String message) {
        super(message);
    }
}

