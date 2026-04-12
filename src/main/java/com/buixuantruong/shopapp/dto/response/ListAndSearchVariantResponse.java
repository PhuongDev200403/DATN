package com.buixuantruong.shopapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListAndSearchVariantResponse {
    private Long id;
    private String productName;
    private Long productId;
    private String imageUrl;
    private BigDecimal price;
    private Long stock;
    private BigDecimal discountPrice;
    private String color;
    private String storage;
}
