package com.buixuantruong.shopapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationDTO {
    private String specName;
    private String specValue;
    private String groupName;
    private Integer displayOrder;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
}
