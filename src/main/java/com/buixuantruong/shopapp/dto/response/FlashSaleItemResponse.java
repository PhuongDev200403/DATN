package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.FlashSaleItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemResponse {
    private Long flashSaleItemId;
    private Long flashSaleId;
    private String flashSaleName;
    private Long variantId;
    private String variantSku;
    private Double originalPrice;
    private Double flashPrice;
    private Integer quantityLimit;
    private Integer quantitySold;
    private Boolean active;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static FlashSaleItemResponse from(FlashSaleItem item) {
        return FlashSaleItemResponse.builder()
                .flashSaleItemId(item.getId())
                .flashSaleId(item.getFlashSale().getId())
                .flashSaleName(item.getFlashSale().getName())
                .variantId(item.getVariant().getId())
                .variantSku(item.getVariant().getSku())
                .originalPrice(item.getOriginalPrice())
                .flashPrice(item.getFlashPrice())
                .quantityLimit(item.getQuantityLimit())
                .quantitySold(item.getQuantitySold())
                .active(item.getActive())
                .startTime(item.getFlashSale().getStartTime())
                .endTime(item.getFlashSale().getEndTime())
                .build();
    }
}
