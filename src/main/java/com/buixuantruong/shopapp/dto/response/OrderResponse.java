package com.buixuantruong.shopapp.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Long userId;
    String fullName;
    String phoneNumber;
    String email;
    String address;
    String note;
    Date orderDate;
    String status;
    BigDecimal totalMoney;
    String shippingMethod;
    String shippingAddress;
    LocalDate shippingDate;
    String paymentMethod;
    String paymentStatus;
    String couponCode;
    BigDecimal discountAmount;
    BigDecimal shippingFee;
    Integer provinceId;
    Integer districtId;
    String wardCode;
    List<OrderDetailResponse> orderDetails;
}
