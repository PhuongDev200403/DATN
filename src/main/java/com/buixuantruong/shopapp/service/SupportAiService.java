package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.model.Order;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.OrderRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupportAiService {
    AiTextService aiTextService;
    OrderRepository orderRepository;
    UserRepository userRepository;

    private static final String SHOP_POLICIES = """
            - Chính sách đổi trả: Trong vòng 7 ngày nếu có lỗi từ nhà sản xuất.
            - Phí vận chuyển: Nội thành Hà Nội 20k, tỉnh khác 30-50k. Miễn phí đơn trên 1 triệu.
            - Thời gian giao hàng: 1-3 ngày làm việc.
            - Bảo hành: Theo chính sách của từng hãng, tối thiểu 6 tháng.
            - Địa chỉ cửa hàng: Số 298 đường Cầu Diễn, Bắc Từ Liêm, Hà Nội.
            - Hotline: 1900-1234 (8:00 - 22:00 hàng ngày).
            """;

    @Cacheable(value = "support_chats", key = "T(java.util.Objects).hash(#query, T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.name)")
    public String chatWithSupport(String query) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String contextContent = "CHÍNH SÁCH CỬA HÀNG:\n" + SHOP_POLICIES;

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String authName = authentication.getName();
            try {
                User user = userRepository.findByPhoneNumber(authName).orElse(null);
                if (user != null) {
                    List<Order> orders = orderRepository.findByUserId(user.getId());
                    if (!orders.isEmpty()) {
                        String orderHistory = orders.stream()
                                .limit(5)
                                .map(o -> String.format("- Đơn #%d: Trạng thái %s, Tổng tiền %,d VNĐ, Ngày đặt %s",
                                        o.getId(), o.getStatus(), o.getTotalMoney(), o.getOrderDate()))
                                .collect(Collectors.joining("\n"));
                        contextContent += "\n\nLỊCH SỬ ĐƠN HÀNG CỦA NGƯỜI DÙNG:\n" + orderHistory;
                    }
                }
            } catch (Exception e) {
                // Unauthenticated or user not found, continue with basic shop policies
            }
        }

        return aiTextService.getSupportResponse(query, contextContent);
    }
}
