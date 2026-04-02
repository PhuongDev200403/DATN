package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.PaymentDTO;
import com.buixuantruong.shopapp.model.Payment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface PaymentService {
    String createPayment(PaymentDTO paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException;
    Payment processVnPayPayment(Map<String, String> vnpParams);
    Payment getPaymentByOrderId(Long orderId);
    Payment save(Payment payment);
} 