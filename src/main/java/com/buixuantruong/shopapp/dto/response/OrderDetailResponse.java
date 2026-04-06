package com.buixuantruong.shopapp.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    Long id;
    Long orderId;
    Float price;
    Integer numberOfProducts;
    Long totalMoney;
    String color;
    VariantResponse variantResponse;
}
