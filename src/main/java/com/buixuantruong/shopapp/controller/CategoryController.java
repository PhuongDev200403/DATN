package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.CategoryDTO;
import com.buixuantruong.shopapp.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("")
    public ApiResponse<Object> createCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                      BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return ApiResponse.builder()
                    .message(String.join(", ", errorMessage))
                    .build();
        }
        return categoryService.createCategory(categoryDTO);

    }

    @GetMapping("")
    public ApiResponse<Object> getAllCategories(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return categoryService.getAllCategories();
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> updateCategory(@PathVariable Long id,
                                                 @RequestBody @Valid CategoryDTO categoryDTO){

        return categoryService.updateCategory(categoryDTO, id);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Object> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return categoryService.deleteCategory(id);
    }
}
