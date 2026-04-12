package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CategoryResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return ApiResponse.<CategoryResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryService.createCategory(categoryDTO))
                .build();
    }

    @GetMapping("")
    public ApiResponse<List<CategoryResponse>> getAllCategories(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryService.getAllCategories())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryDTO categoryDTO) {
        return ApiResponse.<CategoryResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryService.updateCategory(categoryDTO, id))
                .build();
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<MessageResponse> deleteCategory(@PathVariable Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(categoryService.deleteCategory(id))
                .build();
    }
}
