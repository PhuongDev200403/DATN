package com.buixuantruong.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    @Min(value = 1, message = "user's id must be > 0")
    Long userId;
    String fullName;
    String email;
    @NotBlank(message = "phone number is required")
    @Size(min = 10, message = "phone number must be at least 10 characters")
    String phoneNumber;
    String address;
    String note;
    String status;
    @Min(value = 0, message = "total money must be > 0")
    Long totalMoney;
    String shippingMethod;
    String shippingAddress;
    String paymentMethod;
    Boolean paymentSuccess;
    LocalDate shippingDate;
    List<CartItemDTO> cartItems;
    Integer provinceId;
    Integer districtId;
    String wardCode;


}
