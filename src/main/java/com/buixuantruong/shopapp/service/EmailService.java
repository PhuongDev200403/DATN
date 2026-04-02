package com.buixuantruong.shopapp.service;

import java.util.Map;

public interface EmailService {
    /**
     * Gửi email xác nhận đặt hàng thành công
     */
    void sendOrderConfirmation(String toEmail, String customerName, Long orderId, Double totalAmount);

    /**
     * Gửi email cập nhật trạng thái đơn hàng
     */
    void sendOrderStatusUpdate(String toEmail, String customerName, Long orderId, String newStatus);

    /**
     * Gửi email thông báo mã giảm giá mới (nâng cao)
     */
    void sendCouponNotification(String toEmail, String customerName, String couponCode, Double discount);

    /**
     * Gửi email generic với HTML template
     */
    void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables);
}
