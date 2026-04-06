package com.buixuantruong.shopapp.exception;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        StatusCode statusCode = exception.getStatusCode();
        return ResponseEntity.status(statusCode.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(statusCode.getCode())
                        .message(statusCode.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getField() + " is invalid")
                .distinct()
                .reduce((first, second) -> first + ", " + second)
                .orElse(StatusCode.VALIDATION_ERROR.getMessage());

        return ResponseEntity.status(StatusCode.VALIDATION_ERROR.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(StatusCode.VALIDATION_ERROR.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity.status(StatusCode.INVALID_REQUEST.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(StatusCode.INVALID_REQUEST.getCode())
                        .message(exception.getMostSpecificCause() != null
                                ? exception.getMostSpecificCause().getMessage()
                                : StatusCode.INVALID_REQUEST.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(StatusCode.UNAUTHORIZED.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(StatusCode.UNAUTHORIZED.getCode())
                        .message(StatusCode.UNAUTHORIZED.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(StatusCode.INTERNAL_SERVER_ERROR.getHttpStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(StatusCode.INTERNAL_SERVER_ERROR.getCode())
                        .message(StatusCode.INTERNAL_SERVER_ERROR.getMessage())
                        .build());
    }
}
