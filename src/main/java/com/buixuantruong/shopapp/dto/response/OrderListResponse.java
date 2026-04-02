package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Order;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderListResponse {
    List<OrderResponse> orders;
    Integer totalPages;
}
