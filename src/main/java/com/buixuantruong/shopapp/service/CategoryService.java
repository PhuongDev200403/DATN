package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.CategoryDTO;

public interface CategoryService {
    ApiResponse<Object> createCategory(CategoryDTO categoryDTO);

    ApiResponse<Object> getCategoryById(Long id);

    ApiResponse<Object> getAllCategories();

    ApiResponse<Object> updateCategory(CategoryDTO categoryDTO, Long categoryId);

    ApiResponse<Object> deleteCategory(Long id);
}
