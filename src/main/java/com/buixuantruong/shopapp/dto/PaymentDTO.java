package com.buixuantruong.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDTO {
    @JsonProperty("order_id")
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @JsonProperty("payment_method")
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private Double amount;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("transaction_status")
    private String transactionStatus;
    
    @JsonProperty("bank_code")
    private String bankCode;
    
    @JsonProperty("vpc_order_info")
    private String vpcOrderInfo;
} 