package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Category;
import com.buixuantruong.shopapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements com.buixuantruong.shopapp.service.CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ApiResponse<Object> createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .build();
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryRepository.save(newCategory))
                .build();
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public ApiResponse<Object> getCategoryById(Long id) {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Category not found")))
                .build();
    }

    @Override
    @Cacheable(value = "categories")
    public ApiResponse<Object> getAllCategories() {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryRepository.findAll())
                .build();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ApiResponse<Object> updateCategory(CategoryDTO categoryDTO,
                                   Long categoryId) {
        Optional<Category> optionalCategory =categoryRepository.findById(categoryId);
        Category existingCategory = optionalCategory.get();
        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(existingCategory)
                .build();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ApiResponse<Object> deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result("Category deleted successfully")
                .build();
    }
}
