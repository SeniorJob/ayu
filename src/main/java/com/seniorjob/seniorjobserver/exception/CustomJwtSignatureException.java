package com.seniorjob.seniorjobserver.exception;

public class CustomJwtSignatureException extends RuntimeException {
    public CustomJwtSignatureException(String message) {
        super(message);
    }
}
