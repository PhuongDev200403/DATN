package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.CouponType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    Long id;
    String code;
    CouponType type;

    BigDecimal value;
    BigDecimal minimumAmount;
    BigDecimal maxDiscount;
    Integer usageLimit;
    Integer usedCount;

    LocalDateTime startAt;
    LocalDateTime endAt;

    boolean active;

    BigDecimal discountAmount; // dùng khi apply
}
