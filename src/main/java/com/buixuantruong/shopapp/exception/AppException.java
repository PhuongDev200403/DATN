package com.buixuantruong.shopapp.exception;

public class AppException extends RuntimeException{
    public AppException(StatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private StatusCode errorCode;

    public StatusCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(StatusCode errorCode) {
        this.errorCode = errorCode;
    }
}
