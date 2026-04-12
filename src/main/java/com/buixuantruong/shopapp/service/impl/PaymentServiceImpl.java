package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.Configuration.VNPayConfig;
import com.buixuantruong.shopapp.dto.PaymentDTO;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.Payment;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.PaymentRepository;
import com.buixuantruong.shopapp.service.OrderService;
import com.buixuantruong.shopapp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Override
    public String createPayment(PaymentDTO paymentDTO, HttpServletRequest request) throws UnsupportedEncodingException {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new AppException(StatusCode.ORDER_NOT_FOUND));
        if (order.getTotalMoney() == null || order.getTotalMoney().signum() <= 0) {
            throw new AppException(StatusCode.INVALID_REQUEST);
        }

        String vnpVersion = VNPayConfig.vnp_Version;
        String vnpCommand = VNPayConfig.vnp_Command;
        String vnpTmnCode = VNPayConfig.vnp_TmnCode;
        String vnpTxnRef = VNPayConfig.getRandomNumber(8);
        String vnpOrderInfo = "Thanh toan don hang #" + order.getId();
        String vnpOrderType = "other";
        String vnpIpAddr = getIpAddress(request);

        long amount = order.getTotalMoney()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
        String vnpAmount = String.valueOf(amount);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", vnpAmount);
        vnpParams.put("vnp_CurrCode", "VND");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        calendar.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(calendar.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        vnpParams.put("vnp_IpAddr", vnpIpAddr);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_OrderInfo", vnpOrderInfo);
        vnpParams.put("vnp_OrderType", vnpOrderType);
        vnpParams.put("vnp_ReturnUrl", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + VNPayConfig.vnp_ReturnUrl);
        vnpParams.put("vnp_TxnRef", vnpTxnRef);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> iterator = fieldNames.iterator();
        while (iterator.hasNext()) {
            String fieldName = iterator.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (iterator.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String vnpSecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;

        Optional<Payment> existingPayment = paymentRepository.findByOrderId(order.getId());
        Payment payment = existingPayment.orElseGet(() -> Payment.builder()
                .order(order)
                .paymentMethod("VNPAY")
                .build());
        payment.setAmount(order.getTotalMoney());
        payment.setTransactionId(vnpTxnRef);
        payment.setTransactionStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());
        save(payment);

        return paymentUrl;
    }

    @Override
    @Transactional
    public Payment processVnPayPayment(Map<String, String> vnpParams) {
        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnpTransactionStatus = vnpParams.get("vnp_TransactionStatus");
        String vnpBankCode = vnpParams.get("vnp_BankCode");
        String vnpOrderInfo = vnpParams.get("vnp_OrderInfo");

        Optional<Payment> paymentOptional = paymentRepository.findByTransactionId(vnpTxnRef);
        if (paymentOptional.isEmpty() && vnpOrderInfo != null && vnpOrderInfo.contains("#")) {
            String orderIdPart = vnpOrderInfo.substring(vnpOrderInfo.indexOf('#') + 1);
            try {
                Long orderId = Long.parseLong(orderIdPart);
                paymentOptional = paymentRepository.findByOrderId(orderId);
            } catch (NumberFormatException ignored) {
            }
        }

        Payment payment = paymentOptional.orElseThrow(() -> new AppException(StatusCode.PAYMENT_NOT_FOUND));
        boolean paymentSuccessful = "00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus);

        payment.setTransactionStatus(paymentSuccessful ? "SUCCESS" : "FAILED");
        payment.setBankCode(vnpBankCode);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        orderService.finalizeOnlinePayment(payment.getOrder().getId(), paymentSuccessful);
        return payment;
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(StatusCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    private String getIpAddress(HttpServletRequest request) {
        try {
            String forwarded = request.getHeader("X-FORWARDED-FOR");
            return forwarded != null ? forwarded : request.getRemoteAddr();
        } catch (Exception exception) {
            return "127.0.0.1";
        }
    }
}
