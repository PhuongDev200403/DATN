package com.buixuantruong.shopapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponDTO {

    @NotBlank(message = "Coupon code is required")
    String code;

    @NotBlank(message = "Coupon type is required (percentage or fixed)")
    String type;

    @NotNull(message = "Coupon value is required")
    Double value;

    Double minimumAmount;

    LocalDateTime startAt;

    LocalDateTime endAt;

    @Builder.Default
    boolean active = true;
}
