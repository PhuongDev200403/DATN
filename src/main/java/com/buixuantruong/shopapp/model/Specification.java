package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "product_specifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private Variant variant;

    private String specName;

    private String specValue;

    private String groupName;

    private Integer displayOrder;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "length")
    private Integer length;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "ram")
    private Integer ram;
}
