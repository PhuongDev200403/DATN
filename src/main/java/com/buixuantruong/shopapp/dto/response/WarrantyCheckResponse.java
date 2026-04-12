package com.buixuantruong.shopapp.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarrantyCheckResponse {
    String serialNumber;
    Long variantId;
    Long orderId;
    LocalDateTime warrantyExpiredAt;
    boolean inWarranty;
    String status;
}
