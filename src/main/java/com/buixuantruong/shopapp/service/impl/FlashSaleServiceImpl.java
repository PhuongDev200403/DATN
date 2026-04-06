package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.response.FlashSaleItemResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.FlashSaleItem;
import com.buixuantruong.shopapp.model.Variant;
import com.buixuantruong.shopapp.repository.FlashSaleItemRepository;
import com.buixuantruong.shopapp.repository.VariantRepository;
import com.buixuantruong.shopapp.service.FlashSaleService;
import com.buixuantruong.shopapp.mapper.VariantMapper;
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
    VariantMapper variantMapper;

    @Override
    public List<FlashSaleItemResponse> getActiveFlashSaleItems() {
        return flashSaleItemRepository.findActiveFlashSaleItems(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public double getFlashSalePrice(Long variantId) {
        FlashSaleItem flashSaleItem = findCurrentFlashSaleItem(variantId);
        if (flashSaleItem != null) {
            return flashSaleItem.getFlashPrice();
        }

        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(StatusCode.VARIANT_NOT_FOUND));
        return variant.getPrice();
    }

    @Override
    @Transactional
    public void applyFlashSaleWhenCheckout(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new AppException(StatusCode.INVALID_QUANTITY);
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
            throw new AppException(StatusCode.INVALID_REQUEST);
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

    private FlashSaleItemResponse toResponse(FlashSaleItem item) {
        return FlashSaleItemResponse.builder()
                .flashSaleItemId(item.getId())
                .flashSaleId(item.getFlashSale().getId())
                .flashSaleName(item.getFlashSale().getName())
                .variantSku(item.getVariant().getSku())
                .originalPrice(item.getOriginalPrice())
                .flashPrice(item.getFlashPrice())
                .quantityLimit(item.getQuantityLimit())
                .quantitySold(item.getQuantitySold())
                .active(item.getActive())
                .variantResponse(variantMapper.toResponse(item.getVariant()))
                .startTime(item.getFlashSale().getStartTime())
                .endTime(item.getFlashSale().getEndTime())
                .build();
    }
}
