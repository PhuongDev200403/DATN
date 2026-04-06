package com.buixuantruong.shopapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum StatusCode {
    SUCCESS(1000, "Success", HttpStatus.OK),
    INVALID_REQUEST(1001, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(1002, "Validation error", HttpStatus.BAD_REQUEST),
    INVALID_DATA(10021, "Invalid data", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1003, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "Access denied", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(1005, "Resource not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1006, "User not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1007, "Product not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1008, "Category not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(1009, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_DETAIL_NOT_FOUND(1010, "Order detail not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND(1011, "Review not found", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND(1012, "Coupon not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(1013, "Payment not found", HttpStatus.NOT_FOUND),
    VARIANT_NOT_FOUND(1014, "Variant not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(1015, "Cart item not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(1016, "Role not found", HttpStatus.NOT_FOUND),
    USER_EXISTED(1017, "User already exists", HttpStatus.BAD_REQUEST),
    COUPON_CODE_EXISTED(1018, "Coupon code already exists", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1019, "Invalid phone number or password", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1020, "Token is invalid or expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCH(1021, "Password and retype password are not the same", HttpStatus.BAD_REQUEST),
    ADMIN_ROLE_NOT_ALLOWED(1022, "Cannot create user with admin role", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTS(1023, "You have already reviewed this product", HttpStatus.BAD_REQUEST),
    CART_ITEMS_EMPTY(1024, "Cart items cannot be empty", HttpStatus.BAD_REQUEST),
    SHIPPING_DATE_INVALID(1025, "Shipping date cannot be null or in the past", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(1026, "Quantity must be greater than 0", HttpStatus.BAD_REQUEST),
    INVALID_COUPON(1027, "Invalid or inactive coupon", HttpStatus.BAD_REQUEST),
    COUPON_NOT_AVAILABLE(1028, "Coupon is expired or not yet started", HttpStatus.BAD_REQUEST),
    ORDER_AMOUNT_BELOW_MINIMUM(1029, "Order amount is below minimum required", HttpStatus.BAD_REQUEST),
    INVALID_COUPON_TYPE(1030, "Unsupported coupon type", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1031, "File must not be empty", HttpStatus.BAD_REQUEST),
    SOCIAL_PROVIDER_INVALID(1032, "Invalid social provider", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1033, "Category existed", HttpStatus.BAD_REQUEST),
    PRODUCT_EXISTED(1034, "Product existed", HttpStatus.BAD_REQUEST),
    COUPON_USAGE_LIMIT_EXCEEDED(1035, "Coupon usage limit exceeded", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    StatusCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
