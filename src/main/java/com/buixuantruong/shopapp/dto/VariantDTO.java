package com.buixuantruong.shopapp.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Builder
public class VariantDTO {
    private String sku;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Long stock;
    private String color;
    private String storage;
//    private String ram;
    private MultipartFile imageUrl;
    private Boolean isActive;
    private String barcode;
    private Integer weight;
    private Long productId;
    @Valid
    private SpecificationDTO specification;
}
