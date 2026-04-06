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
    //private String ram;
    private String imageUrl;
    private Boolean isActive;
    private String barcode;
    private Integer weight;
    private Long productId;
    private SpecificationResponse specification;

}
