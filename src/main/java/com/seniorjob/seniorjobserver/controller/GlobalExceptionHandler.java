package com.seniorjob.seniorjobserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        String userFriendlyMessage = "잘못된 요청입니다";
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, userFriendlyMessage, ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(NoSuchFieldError.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchFieldError ex){
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND,
                "Resource not found", ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 예외 정보 : " + ex.getMessage()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError){
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }


    public static class ApiError{
        private HttpStatus status;
        private String message;
        private String detail;

        public ApiError(HttpStatus status, String message){
            this.status = status;
            this.message = message;
        }

        public ApiError(HttpStatus status, String message, String detail){
            this.status = status;
            this.message = message;
            this.detail = detail;
        }

        public  HttpStatus getStatus(){
            return status;
        }
        public  String getMessage(){
            return message;
        }
        public String getDetail(){
            return detail;
        }

    }

}
