package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.model.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = VariantMapper.class)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "dateRelease", ignore = true)
    @Mapping(target = "isOnSale", ignore = true)
    Product toProduct(ProductDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "dateRelease", ignore = true)
    @Mapping(target = "isOnSale", ignore = true)
    void updateProductFromDto(ProductDTO dto, @MappingTarget Product product);

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toResponse(Product product);
}
