package com.buixuantruong.shopapp.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Long id;
    Long orderId;
    BigDecimal price;
    Integer numberOfProducts;
    BigDecimal totalMoney;
    String color;
    VariantResponse variantResponse;
}
