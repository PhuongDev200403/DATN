package com.buixuantruong.shopapp.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final StatusCode statusCode;

    public AppException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }

    public AppException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
