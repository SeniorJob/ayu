package com.seniorjob.seniorjobserver.exception;

public class CustomJwtIllegalArgumentException extends RuntimeException {
    public CustomJwtIllegalArgumentException(String message) {
        super(message);
    }
}
