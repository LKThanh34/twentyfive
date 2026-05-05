package com.project.DuAnTotNghiep.exception;

import org.springframework.http.HttpStatus;

public class ShopApiException extends RuntimeException{
    private HttpStatus status;
    private String message;

    public ShopApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ShopApiException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
