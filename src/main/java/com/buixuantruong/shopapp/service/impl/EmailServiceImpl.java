package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailServiceImpl implements EmailService {

    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    @NonFinal
    String fromEmail;

    @Override
    @Async
    public void sendOrderConfirmation(String toEmail, String customerName, Long orderId, Double totalAmount) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("totalAmount", String.format("%,.0f", totalAmount) + " VNĐ");

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            sendEmail(toEmail, "✅ Xác nhận đặt hàng #" + orderId + " - ShopApp", htmlContent);
            log.info("Order confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdate(String toEmail, String customerName, Long orderId, String newStatus) {
        try {
            String statusVi = translateStatus(newStatus);
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("newStatus", statusVi);

            String htmlContent = templateEngine.process("email/order-status-update", context);
            sendEmail(toEmail, "📦 Cập nhật đơn hàng #" + orderId + ": " + statusVi, htmlContent);
            log.info("Order status update email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send order status email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendCouponNotification(String toEmail, String customerName, String couponCode, Double discount) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("couponCode", couponCode);
            context.setVariable("discount", discount);

            String htmlContent = templateEngine.process("email/coupon-notification", context);
            sendEmail(toEmail, "🎁 Mã giảm giá đặc biệt dành cho bạn!", htmlContent);
            log.info("Coupon notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send coupon email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);
            sendEmail(toEmail, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, "ShopApp");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private String translateStatus(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING"    -> "Chờ xác nhận";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPING"   -> "Đang giao hàng";
            case "DELIVERED"  -> "Đã giao hàng";
            case "CANCELLED"  -> "Đã hủy";
            default           -> status;
        };
    }
}
