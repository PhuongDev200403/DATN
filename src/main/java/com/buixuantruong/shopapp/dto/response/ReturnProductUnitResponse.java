package com.buixuantruong.shopapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReturnProductUnitResponse {
    String serialNumber;
    Long orderId;
    Long variantId;
    String unitStatus;
    LocalDateTime returnedAt;
    String message;
}
