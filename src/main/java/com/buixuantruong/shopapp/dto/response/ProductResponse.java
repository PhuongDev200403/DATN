package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Product;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse extends BaseResponse {
    Long id;
    String name;
    float price;
    String thumbnail;
    String description;
    Long categoryId;
    Long quantity;
    String warrantyCode;
    @Builder.Default
    List<VariantResponse> variants = new ArrayList<>();

    public static ProductResponse from(Product product) {
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .thumbnail(product.getThumbnail())
                .price(product.getPrice() != null ? product.getPrice() : 0F)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .quantity(product.getQuantity())
                .warrantyCode(product.getWarrantyCode())
                .variants(product.getVariants() == null ? new ArrayList<>() :
                        product.getVariants().stream().map(VariantResponse::from).toList())
                .build();
        productResponse.setCreateAt(product.getCreateAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());
        return productResponse;
    }
}
