package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "warrantyCode", ignore = true)
    @Mapping(target = "dateRelease", ignore = true)
    @Mapping(target = "isOnSale", ignore = true)
    Product toProduct(ProductDTO dto);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "variants", ignore = true)
    ProductResponse toResponse(Product product);
}
