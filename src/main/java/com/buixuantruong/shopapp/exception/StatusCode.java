package com.buixuantruong.shopapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum StatusCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    //INVALID_CREDENTIALS(1001, "Invalid_credential", HttpStatus.UNAUTHORIZED),
    FILE_NOT_FOUND(1002, "File not found", HttpStatus.NOT_FOUND),
    INVALID_DATA(1003, "Invalid data", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1004, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not found in database", HttpStatus.NOT_FOUND),
    BAD_REQUEST(1006, "Bad request", HttpStatus.BAD_REQUEST),
    INVALID_PARAM(1007, "Invalid param", HttpStatus.INTERNAL_SERVER_ERROR),
    UNCATEGORIZED(9999, "Uncategoried", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(100, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(101, "Token không hợp lệ", HttpStatus.UNAUTHORIZED);



    private final int code;
    private final HttpStatusCode httpStatusCode;
    private String message;
    StatusCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
