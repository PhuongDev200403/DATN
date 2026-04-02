package com.buixuantruong.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDTO {

    @Min(value = 1, message = "order's id must be higher 0")
    private Long orderId;


    @Min(value = 1, message = "product's id must be higher 0")
    private Long productId;

    @Min(value = 1, message = "price must be higher 0")
    private Float price;


    @Min(value = 1, message = "number id must be higher 1")
    private int numberOfProducts;


    @Min(value = 0, message = "total money must be > 0")
    private Long totalMoney;

    private String color;
}
