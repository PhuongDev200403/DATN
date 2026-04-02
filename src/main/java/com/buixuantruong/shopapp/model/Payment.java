package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // VNPAY, COD, CREDIT_CARD

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "transaction_status")
    private String transactionStatus;

    @Column(name = "bank_code")
    private String bankCode;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
} 