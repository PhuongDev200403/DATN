package com.buixuantruong.shopapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    Long id;
    String code;
    String type;
    Double value;
    Double minimumAmount;
    LocalDateTime startAt;
    LocalDateTime endAt;
    boolean active;
    Double discountAmount; // Chỉ dùng khi API "apply" để trả về số tiền được giảm
}
