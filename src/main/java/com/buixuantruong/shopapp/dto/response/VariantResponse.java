package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Variant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {
    private Long id;
    private String sku;
    private Float price;
    private Float discountPrice;
    private Long stock;
    private String color;
    private String storage;
    private String ram;
    private String imageUrl;
    private Boolean isActive;
    private String barcode;
    private Integer weight;
    private SpecificationResponse specification;

    public static VariantResponse from(Variant variant) {
        if (variant == null) {
            return null;
        }
        return VariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .discountPrice(variant.getDiscountPrice())
                .stock(variant.getStock())
                .color(variant.getColor())
                .storage(variant.getStorage())
                .ram(variant.getRam())
                .imageUrl(variant.getImageUrl())
                .isActive(variant.getIsActive())
                .barcode(variant.getBarcode())
                .weight(variant.getWeight())
                .specification(SpecificationResponse.from(variant.getSpecification()))
                .build();
    }
}
