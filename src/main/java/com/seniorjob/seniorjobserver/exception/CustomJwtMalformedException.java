package com.seniorjob.seniorjobserver.exception;

public class CustomJwtMalformedException extends RuntimeException {
    public CustomJwtMalformedException(String message) {
        super(message);
    }
}
