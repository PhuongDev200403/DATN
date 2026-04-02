package com.buixuantruong.shopapp.component;

import com.buixuantruong.shopapp.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupJob {

    private final CartItemRepository cartItemRepository;

    // Chạy vào 2h sáng mỗi ngày
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupInactiveCarts() {
        log.info("Starting inactive cart cleanup job...");
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        cartItemRepository.deleteInactiveCartItems(threshold);
        log.info("Inactive cart cleanup job finished.");
    }
}
