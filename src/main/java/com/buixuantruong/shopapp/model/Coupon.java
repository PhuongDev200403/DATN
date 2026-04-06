package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CouponType type;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal value;

    @Column(nullable = false)
    Integer usageLimit;

    @Builder.Default
    @Column(nullable = false)
    Integer usedCount = 0;

    @Column(precision = 15, scale = 2)
    BigDecimal minimumAmount;

    @Column(precision = 15, scale = 2)
    BigDecimal maxDiscount;

    LocalDateTime startAt;

    LocalDateTime endAt;

    @Builder.Default
    boolean active = true;
}
