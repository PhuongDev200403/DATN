package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.dto.response.CategoryResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.mapper.CategoryMapper;
import com.buixuantruong.shopapp.model.Category;
import com.buixuantruong.shopapp.repository.CategoryRepository;
import com.buixuantruong.shopapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryDTO categoryDTO) {
        String normalizedName = categoryDTO.getName() == null ? null : categoryDTO.getName().trim();
        if (normalizedName != null && categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new AppException(StatusCode.CATEGORY_EXISTED);
        }

        Category newCategory = categoryMapper.toCategory(categoryDTO);
        newCategory.setName(normalizedName);
        return categoryMapper.toCategoryResponse(categoryRepository.save(newCategory));
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(StatusCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Cacheable(value = "categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(StatusCode.CATEGORY_NOT_FOUND));
        String normalizedName = categoryDTO.getName() == null ? null : categoryDTO.getName().trim();
        boolean isDuplicatedName = normalizedName != null
                && !existingCategory.getName().equalsIgnoreCase(normalizedName)
                && categoryRepository.existsByNameIgnoreCase(normalizedName);
        if (isDuplicatedName) {
            throw new AppException(StatusCode.CATEGORY_EXISTED);
        }

        existingCategory.setName(normalizedName);
        return categoryMapper.toCategoryResponse(categoryRepository.save(existingCategory));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public MessageResponse deleteCategory(Long id) {
        categoryRepository.deleteById(id);
        return MessageResponse.builder().message("Category deleted successfully").build();
    }
}
