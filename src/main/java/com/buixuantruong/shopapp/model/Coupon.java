package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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

    @Column(name = "code", nullable = false, unique = true)
    String code;

    @Column(name = "type", nullable = false)
    CouponType type; // "percentage" hoặc "fixed"

    @Column(name = "value", nullable = false)
    Double value; // Giá trị giảm (vd: 10.0 cho 10% hoặc 50000.0 cho tiền mặt)

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "use_count", nullable = false)
    private Integer usedCount;

    @Column(name = "minimum_amount")
    Double minimumAmount; // Giá trị đơn hàng tối thiểu để áp dụng

    @Column(name = "start_at")
    LocalDateTime startAt;

    @Column(name = "end_at")
    LocalDateTime endAt;

    @Column(name = "is_active")
    @Builder.Default
    boolean active = true;
}
