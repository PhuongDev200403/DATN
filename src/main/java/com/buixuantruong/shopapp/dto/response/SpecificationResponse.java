package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Specification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationResponse {
    private Long id;
    private String specName;
    private String specValue;
    private String groupName;
    private Integer displayOrder;
    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
    private Integer ram;

//    public static SpecificationResponse from(Specification specification) {
//        if (specification == null) {
//            return null;
//        }
//        return SpecificationResponse.builder()
//                .id(specification.getId())
//                .specName(specification.getSpecName())
//                .specValue(specification.getSpecValue())
//                .groupName(specification.getGroupName())
//                .displayOrder(specification.getDisplayOrder())
//                .weight(specification.getWeight())
//                .length(specification.getLength())
//                .width(specification.getWidth())
//                .height(specification.getHeight())
//                .build();
//    }
}
