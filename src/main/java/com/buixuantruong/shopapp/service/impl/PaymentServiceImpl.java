package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.Configuration.VNPayConfig;
import com.buixuantruong.shopapp.dto.PaymentDTO;
import com.buixuantruong.shopapp.exception.ResourceNotFoundException;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.Payment;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.PaymentRepository;
import com.buixuantruong.shopapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public String createPayment(PaymentDTO paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentDTO.getOrderId()));

        String vnp_Version = VNPayConfig.vnp_Version;
        String vnp_Command = VNPayConfig.vnp_Command;
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_OrderInfo = "Thanh toan don hang #" + order.getId();
        String vnp_OrderType = "other";
        String vnp_IpAddr = getIpAddress(request);
        
        // Convert amount to VNĐ (x100 because VNPay requires amount in cents)
        long amount = Math.round(paymentDTO.getAmount() * 100);
        String vnp_Amount = String.valueOf(amount);
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        
        // Get current time in GMT+7
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        // Calculate expiry date (15 minutes from now)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_ReturnUrl", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        
        // Build query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        
        // Kiểm tra xem đã tồn tại payment nào cho order này chưa
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(order.getId());
        
        Payment payment;
        if (existingPayment.isPresent()) {
            // Nếu đã tồn tại payment, cập nhật thông tin mới
            payment = existingPayment.get();
            payment.setAmount(paymentDTO.getAmount());
            payment.setTransactionId(vnp_TxnRef);
            payment.setTransactionStatus("PENDING");
            payment.setPaymentDate(LocalDateTime.now());
        } else {
            // Nếu chưa tồn tại, tạo payment mới
            payment = Payment.builder()
                    .order(order)
                    .paymentMethod("VNPAY")
                    .amount(paymentDTO.getAmount())
                    .transactionId(vnp_TxnRef)
                    .transactionStatus("PENDING")
                    .paymentDate(LocalDateTime.now())
                    .build();
        }
        
        save(payment);
        
        return paymentUrl;
    }

    @Override
    @Transactional
    public Payment processVnPayPayment(Map<String, String> vnpParams) {
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TransactionStatus = vnpParams.get("vnp_TransactionStatus");
        String vnp_BankCode = vnpParams.get("vnp_BankCode");
        String vnp_OrderInfo = vnpParams.get("vnp_OrderInfo");
        
        // Tìm payment bằng transactionId trước
        Optional<Payment> paymentOptional = paymentRepository.findByTransactionId(vnp_TxnRef);
        
        // Nếu không tìm thấy qua transactionId, thử tìm qua orderId từ vnp_OrderInfo
        if (paymentOptional.isEmpty() && vnp_OrderInfo != null && vnp_OrderInfo.contains("#")) {
            try {
                String orderIdStr = vnp_OrderInfo.substring(vnp_OrderInfo.indexOf("#") + 1);
                Long orderId = Long.parseLong(orderIdStr);
                System.out.println("Extracted orderId from vnp_OrderInfo: " + orderId);
                
                // Thử tìm payment qua orderId
                paymentOptional = paymentRepository.findByOrderId(orderId);
                System.out.println("Looking for payment by orderId: " + orderId);
            } catch (Exception e) {
                System.out.println("Could not extract or process orderId from vnp_OrderInfo: " + vnp_OrderInfo);
            }
        }
        
        // Nếu vẫn không tìm thấy, throw exception
        if (paymentOptional.isEmpty()) {
            System.out.println("Payment not found with transaction ID: " + vnp_TxnRef + " or from order info: " + vnp_OrderInfo);
            throw new ResourceNotFoundException("Payment not found with transaction ID: " + vnp_TxnRef);
        }
        
        Payment payment = paymentOptional.get();
        
        // Update payment status based on VNPay response
        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            payment.setTransactionStatus("SUCCESS");
            payment.setBankCode(vnp_BankCode);
            payment.setPaymentDate(LocalDateTime.now());
            
            // Update order status
            Order order = payment.getOrder();
            order.setStatus("PAID");
            orderRepository.save(order);
        } else {
            payment.setTransactionStatus("FAILED");
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
} 