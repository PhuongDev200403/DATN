package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.SpecificationDTO;
import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CloudinaryUploadResponse;
import com.buixuantruong.shopapp.dto.response.ProductListResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.service.CloudinaryService;
import com.buixuantruong.shopapp.service.ProductService;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    CloudinaryService cloudinaryService;

    @PostMapping("")
    public ApiResponse<Object> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                List<String> errorMessage = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ApiResponse.builder()
                        .message(String.join(", ", errorMessage))
                        .build();
            }
            return ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result(productService.createProduct(productDTO).getResult())
                    .build();
        } catch (Exception e) {
            return ApiResponse.builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    @GetMapping("/ai-assistant")
    public ResponseEntity<ApiResponse<Object>> aiAssistant(@RequestParam String query) {
        try {
            String aiResponse = productService.getAiAssistantResponse(query);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result(aiResponse)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .code(StatusCode.INVALID_PARAM.getCode())
                            .message("AI Assistant failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<Object>> generateDescription(
            @RequestParam String productName,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String specs) {
        try {
            String description = productService.generateProductDescription(productName, categoryId, specs);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result(description)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .code(StatusCode.INVALID_PARAM.getCode())
                            .message("AI Generation failed: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("")
    public ApiResponse<Object> getProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "min_price", required = false) Float minPrice,
            @RequestParam(value = "max_price", required = false) Float maxPrice,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();

        PageRequest pageRequest = PageRequest.of(page, limit, sortObj);

        Page<ProductResponse> products = productService.searchProducts(
                categoryId, keyword, minPrice, maxPrice, pageRequest);

        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(new ProductListResponse(products.getContent(), products.getTotalPages()))
                .build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<Object>> uploadImage(@RequestPart("file") MultipartFile file) {
        try {
            CloudinaryUploadResponse response = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(StatusCode.SUCCESS.getCode())
                    .message(StatusCode.SUCCESS.getMessage())
                    .result(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .code(StatusCode.BAD_REQUEST.getCode())
                            .message("Upload failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts() throws Exception {
        Faker faker = new Faker();
        for (int i = 0; i < 1000; i++) {
            String productName = faker.commerce().productName();
            if (productService.existsProduct(productName)) {
                continue;
            }

            VariantDTO variant = VariantDTO.builder()
                    .sku("FAKE-" + faker.number().digits(8))
                    .price((float) faker.number().numberBetween(10, 900000000))
                    .stock((long) faker.number().numberBetween(1, 100))
                    .imageUrl("https://picsum.photos/200/300")
                    .isActive(true)
                    .specification(SpecificationDTO.builder()
                            .width(faker.number().numberBetween(12, 15))
                            .height(faker.number().numberBetween(100, 500))
                            .weight(faker.number().numberBetween(120, 300))
                            .length(faker.number().numberBetween(120, 400))
                            .build())
                    .build();

            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .description(faker.lorem().sentence(10))
                    .thumbnail("https://picsum.photos/200/300")
                    .price(variant.getPrice())
                    .categoryId((long) faker.number().numberBetween(1, 5))
                    .quantity(variant.getStock())
                    .variants(List.of(variant))
                    .build();
            productService.createProduct(productDTO);
        }
        return ResponseEntity.ok("Fake products generated successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getProductById(id))
                .build();
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<Object> getSimilarProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getSimilarProducts(id))
                .build();
    }

    @GetMapping("/{id}/frequently-bought-together")
    public ApiResponse<Object> getFrequentlyBoughtTogetherProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getFrequentlyBoughtTogetherProducts(id))
                .build();
    }

    @GetMapping("/top-selling")
    public ApiResponse<Object> getTopSellingProducts() {
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getTopSellingProducts())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ApiResponse.builder()
                .result("Delete product successfully")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) throws Exception {
        ProductResponse updatedProduct = productService.updateProduct(id, productDTO);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(updatedProduct)
                .build();
    }

    @GetMapping("/by-ids")
    public ApiResponse<Object> getProductsByIds(@RequestParam("ids") String ids) {
        List<Long> productIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        List<Product> products = productService.findProductByIds(productIds);
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(products)
                .build();
    }

    @DeleteMapping("/deleteFakeProducts")
    public ResponseEntity<String> deleteFakeProducts() {
        productService.deleteFakeProducts();
        return ResponseEntity.ok("Deleted fake products successfully");
    }
}
