package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CloudinaryUploadResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.ProductListResponse;
import com.buixuantruong.shopapp.dto.response.ProductResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.ProductService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    Cloudinary cloudinary;

    @Value("${cloudinary.folder:shopapp}")
    @NonFinal
    String cloudinaryFolder;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            throw new AppException(StatusCode.VALIDATION_ERROR);
        }
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.createProduct(productDTO))
                .build();
    }

    @GetMapping("/ai-assistant")
    public ResponseEntity<ApiResponse<String>> aiAssistant(@RequestParam String query) throws Exception {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getAiAssistantResponse(query))
                .build());
    }

    @PostMapping("/generate-description")
    public ResponseEntity<ApiResponse<String>> generateDescription(@RequestParam String productName, @RequestParam Long categoryId, @RequestParam(required = false) String specs) throws Exception {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.generateProductDescription(productName, categoryId, specs))
                .build());
    }

    @GetMapping("")
    public ApiResponse<ProductListResponse> getProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "min_price", required = false) Float minPrice,
            @RequestParam(value = "max_price", required = false) Float maxPrice,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ? Sort.by(sort).descending() : Sort.by(sort).ascending();
        PageRequest pageRequest = PageRequest.of(page, limit, sortObj);
        Page<ProductResponse> products = productService.searchProducts(categoryId, keyword, minPrice, maxPrice, pageRequest);
        return ApiResponse.<ProductListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(new ProductListResponse(products.getContent(), products.getTotalPages()))
                .build();
    }

    @GetMapping("/new")
    public ApiResponse<ProductListResponse> getNewProducts(
            @RequestParam(value = "months", defaultValue = "6") int months,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "sort", defaultValue = "createAt") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        Sort sortObj = direction.equalsIgnoreCase("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        PageRequest pageRequest = PageRequest.of(page, limit, sortObj);
        Page<ProductResponse> products = productService.getNewProducts(months, pageRequest);
        return ApiResponse.<ProductListResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(new ProductListResponse(products.getContent(), products.getTotalPages()))
                .build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<CloudinaryUploadResponse>> uploadImage(@RequestPart("file") MultipartFile file) throws java.io.IOException {
        return ResponseEntity.ok(ApiResponse.<CloudinaryUploadResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(uploadProductImage(file))
                .build());
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
    public ApiResponse<List<ProductResponse>> getSimilarProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getSimilarProducts(id))
                .build();
    }

    @GetMapping("/{id}/frequently-bought-together")
    public ApiResponse<List<ProductResponse>> getFrequentlyBoughtTogetherProducts(@PathVariable("id") Long id) throws Exception {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getFrequentlyBoughtTogetherProducts(id))
                .build();
    }

    @GetMapping("/top-selling")
    public ApiResponse<List<ProductResponse>> getTopSellingProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.getTopSellingProducts())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteProduct(@PathVariable("id") Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.deleteProduct(id))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) throws Exception {
        return ApiResponse.<ProductResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.updateProduct(id, productDTO))
                .build();
    }

    @GetMapping("/by-ids")
    public ApiResponse<List<ProductResponse>> getProductsByIds(@RequestParam("ids") String ids) {
        List<Long> productIds = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        return ApiResponse.<List<ProductResponse>>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(productService.findProductByIds(productIds))
                .build();
    }

    @DeleteMapping("/deleteFakeProducts")
    public ApiResponse<MessageResponse> deleteFakeProducts() {
        productService.deleteFakeProducts();
        return ApiResponse.<MessageResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(MessageResponse.builder().message("Deleted fake products successfully").build())
                .build();
    }

    private CloudinaryUploadResponse uploadProductImage(MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(StatusCode.FILE_EMPTY);
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", cloudinaryFolder,
                        "resource_type", "image"
                )
        );

        return CloudinaryUploadResponse.builder()
                .url(String.valueOf(uploadResult.get("secure_url")))
                .publicId(String.valueOf(uploadResult.get("public_id")))
                .originalFilename(file.getOriginalFilename())
                .build();
    }
}
