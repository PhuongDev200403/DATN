package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.dto.response.CategoryResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryDTO categoryDTO);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(CategoryDTO categoryDTO, Long categoryId);

    MessageResponse deleteCategory(Long id);
}
