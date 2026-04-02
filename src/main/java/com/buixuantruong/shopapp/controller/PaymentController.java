package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.PaymentDTO;
import com.buixuantruong.shopapp.model.Payment;
import com.buixuantruong.shopapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request
    ) {
        try {
            System.out.println("Received payment request: " + paymentDTO);
            System.out.println("Order ID: " + paymentDTO.getOrderId() + ", Class: " + (paymentDTO.getOrderId() != null ? paymentDTO.getOrderId().getClass().getName() : "null"));
            System.out.println("Payment Method: " + paymentDTO.getPaymentMethod());
            System.out.println("Amount: " + paymentDTO.getAmount() + ", Class: " + (paymentDTO.getAmount() != null ? paymentDTO.getAmount().getClass().getName() : "null"));
            
            // Kiểm tra orderId
            if (paymentDTO.getOrderId() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Order ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Kiểm tra amount
            if (paymentDTO.getAmount() == null || paymentDTO.getAmount() <= 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Amount must be greater than 0");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String paymentUrl = paymentService.createPayment(paymentDTO, request);
            System.out.println("Generated payment URL: " + paymentUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("payment_url", paymentUrl);
            return ResponseEntity.ok(response);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/vnpay-payment-callback")
    public ResponseEntity<?> vnpayPaymentCallback(
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request
    ) {
        // Process the payment result from VNPay
        Payment payment = paymentService.processVnPayPayment(queryParams);
        
        // Redirect to frontend with appropriate status
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
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }
} 