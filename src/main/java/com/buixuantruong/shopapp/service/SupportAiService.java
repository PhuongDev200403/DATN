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
            - ChÃ­nh sÃ¡ch Ä‘á»•i tráº£: Trong vÃ²ng 7 ngÃ y náº¿u cÃ³ lá»—i tá»« nhÃ  sáº£n xuáº¥t.
            - PhÃ­ váº­n chuyá»ƒn: Ná»™i thÃ nh HÃ  Ná»™i 20k, tá»‰nh khÃ¡c 30-50k. Miá»…n phÃ­ Ä‘Æ¡n trÃªn 1 triá»‡u.
            - Thá»i gian giao hÃ ng: 1-3 ngÃ y lÃ m viá»‡c.
            - Báº£o hÃ nh: Theo chÃ­nh sÃ¡ch cá»§a tá»«ng hÃ£ng, tá»‘i thiá»ƒu 6 thÃ¡ng.
            - Äá»‹a chá»‰ cá»­a hÃ ng: Sá»‘ 298 Ä‘Æ°á»ng Cáº§u Diá»…n, Báº¯c Tá»« LiÃªm, HÃ  Ná»™i.
            - Hotline: 1900-1234 (8:00 - 22:00 hÃ ng ngÃ y).
            """;

    @Cacheable(value = "support_chats", key = "T(java.util.Objects).hash(#query, T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.name)")
    public String chatWithSupport(String query) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String contextContent = "CHÃNH SÃCH Cá»¬A HÃ€NG:\n" + SHOP_POLICIES;

        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String authName = authentication.getName();
            try {
                User user = userRepository.findByPhoneNumber(authName).orElse(null);
                if (user != null) {
                    List<Order> orders = orderRepository.findByUserId(user.getId());
                    if (!orders.isEmpty()) {
                        String orderHistory = orders.stream()
                                .limit(5)
                                .map(order -> String.format(
                                        "- ÄÆ¡n #%d: Tráº¡ng thÃ¡i %s, Tá»•ng tiá»n %s VNÄ, NgÃ y Ä‘áº·t %s",
                                        order.getId(),
                                        order.getStatus(),
                                        order.getTotalMoney(),
                                        order.getOrderDate()
                                ))
                                .collect(Collectors.joining("\n"));
                        contextContent += "\n\nLá»ŠCH Sá»¬ ÄÆ N HÃ€NG Cá»¦A NGÆ¯á»œI DÃ™NG:\n" + orderHistory;
                    }
                }
            } catch (Exception ignored) {
                // Continue with basic shop policies when user context is unavailable.
            }
        }

        return aiTextService.getSupportResponse(query, contextContent);
    }
}
