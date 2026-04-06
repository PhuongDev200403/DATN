package com.buixuantruong.shopapp.dto;

import com.buixuantruong.shopapp.model.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponDTO {

    @NotBlank(message = "Coupon code is required")
    String code;

    @NotNull(message = "Coupon type is required")
    CouponType type;

    @NotNull(message = "Coupon value is required")
    BigDecimal value;

    BigDecimal minimumAmount;

    BigDecimal maxDiscount;

    Integer usageLimit;

    LocalDateTime startAt;

    LocalDateTime endAt;

    @Builder.Default
    boolean active = true;
}
