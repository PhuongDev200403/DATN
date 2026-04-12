package com.buixuantruong.shopapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReturnProductUnitRequest {
    @NotNull(message = "orderId is required")
    Long orderId;

    @NotBlank(message = "serialNumber is required")
    String serialNumber;
}
