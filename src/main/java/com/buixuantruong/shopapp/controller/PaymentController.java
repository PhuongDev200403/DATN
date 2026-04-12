package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.PaymentDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Payment;
import com.buixuantruong.shopapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<Map<String, String>> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request
    ) throws UnsupportedEncodingException {
        if (paymentDTO.getOrderId() == null) {
            throw new AppException(StatusCode.INVALID_REQUEST);
        }
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount().signum() <= 0) {
            throw new AppException(StatusCode.INVALID_REQUEST);
        }

        String paymentUrl = paymentService.createPayment(paymentDTO, request);
        return ApiResponse.<Map<String, String>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(Map.of("payment_url", paymentUrl))
                .build();
    }

    @GetMapping("/vnpay-payment-callback")
    public ResponseEntity<Void> vnpayPaymentCallback(
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request
    ) {
        Payment payment = paymentService.processVnPayPayment(queryParams);

        String frontendUrl = "http://localhost:4200/payment-result";
        String status = "FAILED";
        if ("SUCCESS".equals(payment.getTransactionStatus())) {
            status = "SUCCESS";
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", frontendUrl + "?status=" + status + "&orderId=" + payment.getOrder().getId())
                .build();
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        return ApiResponse.<Payment>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(paymentService.getPaymentByOrderId(orderId))
                .build();
    }
}
