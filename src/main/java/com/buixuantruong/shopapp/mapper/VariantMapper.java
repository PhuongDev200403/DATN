package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import com.buixuantruong.shopapp.model.Variant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = SpecificationMapper.class)
public interface VariantMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "specification", ignore = true)
    Variant toVariant(VariantDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "specification", ignore = true)
    void updateVariantFromDto(VariantDTO dto, @MappingTarget Variant variant);

    @Mapping(target = "productId", source = "product.id")
    VariantResponse toResponse(Variant variant);
}
