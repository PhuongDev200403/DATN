package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.response.FlashSaleItemResponse;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.model.FlashSaleItem;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.FlashSaleItemRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.FlashSaleService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleServiceImpl implements FlashSaleService {

    FlashSaleItemRepository flashSaleItemRepository;
    VariantRepository variantRepository;

    @Override
    public List<FlashSaleItemResponse> getActiveFlashSaleItems() {
        return flashSaleItemRepository.findActiveFlashSaleItems(LocalDateTime.now()).stream()
                .map(FlashSaleItemResponse::from)
                .toList();
    }

    @Override
    public double getFlashSalePrice(Long variantId) {
        FlashSaleItem flashSaleItem = findCurrentFlashSaleItem(variantId);
        if (flashSaleItem != null) {
            return flashSaleItem.getFlashPrice();
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with id = " + variantId));
        return variant.getPrice();
    }

    @Override
    @Transactional
    public void applyFlashSaleWhenCheckout(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        List<FlashSaleItem> lockedItems = flashSaleItemRepository.lockValidFlashSaleItemsByVariantId(
                variantId,
                LocalDateTime.now()
        );

        if (lockedItems.isEmpty()) {
            return;
        }

        FlashSaleItem flashSaleItem = lockedItems.getFirst();
        int quantitySold = flashSaleItem.getQuantitySold() != null ? flashSaleItem.getQuantitySold() : 0;
        int quantityLimit = flashSaleItem.getQuantityLimit() != null ? flashSaleItem.getQuantityLimit() : 0;

        if (quantitySold + quantity > quantityLimit) {
            throw new IllegalStateException("Flash sale het hang");
        }

        // Lock + transactional update to prevent oversell when multiple checkouts happen concurrently.
        flashSaleItem.setQuantitySold(quantitySold + quantity);
        flashSaleItemRepository.save(flashSaleItem);
    }

    private FlashSaleItem findCurrentFlashSaleItem(Long variantId) {
        List<FlashSaleItem> items = flashSaleItemRepository.findValidFlashSaleItemsByVariantId(
                variantId,
                LocalDateTime.now()
        );
        return items.isEmpty() ? null : items.getFirst();
    }
}
