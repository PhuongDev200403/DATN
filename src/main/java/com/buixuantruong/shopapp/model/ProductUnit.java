package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_units",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_unit_serial", columnNames = "serial_number")
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUnit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "serial_number", nullable = false, length = 100)
    String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ProductUnitStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    Variant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @Column(name = "warranty_expired_at")
    LocalDateTime warrantyExpiredAt;

    @Column(name = "sold_at")
    LocalDateTime soldAt;

    @Column(name = "returned_at")
    LocalDateTime returnedAt;

    @Version
    Long version;
}
