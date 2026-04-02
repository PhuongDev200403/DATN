package com.buixuantruong.shopapp.exception;
public class ExpiredTokenException extends Exception {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
