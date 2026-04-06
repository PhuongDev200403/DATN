package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.dto.response.CategoryResponse;
import com.buixuantruong.shopapp.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryDTO categoryDTO);

    CategoryResponse toCategoryResponse(Category category);
}
