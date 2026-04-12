package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.VariantDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CloudinaryUploadResponse;
import com.buixuantruong.shopapp.dto.response.MessageResponse;
import com.buixuantruong.shopapp.dto.response.VariantResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.service.VariantService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.buixuantruong.shopapp.exception.StatusCode.SUCCESS;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantController {
    VariantService variantService;
    Cloudinary cloudinary;

    @Value("${cloudinary.folder:shopapp}")
    @NonFinal
    String cloudinaryFolder;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VariantResponse> createVariant(
            @ModelAttribute VariantDTO variantDTO,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.createVariant(variantDTO, file))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VariantResponse> getVariantById(@PathVariable Long id) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.getVariantById(id))
                .build();
    }

    @GetMapping("")
    public ApiResponse<List<VariantResponse>> getVariants(@RequestParam(value = "productId", required = false) Long productId) {
        List<VariantResponse> result = productId == null
                ? variantService.getAllVariants()
                : variantService.getVariantsByProductId(productId);
        return ApiResponse.<List<VariantResponse>>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(result)
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VariantResponse> updateVariant(
            @PathVariable Long id,
            @ModelAttribute VariantDTO variantDTO,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updateVariant(id, variantDTO, file))
                .build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<CloudinaryUploadResponse>> uploadImage(@RequestPart("file") MultipartFile file) throws java.io.IOException {
        return ResponseEntity.ok(ApiResponse.<CloudinaryUploadResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(uploadVariantImage(file))
                .build());
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<VariantResponse> updateStock(@PathVariable Long id, @RequestParam Long newStock) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updateStock(id, newStock))
                .build();
    }

    @PatchMapping("/{id}/price")
    public ApiResponse<VariantResponse> updatePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) BigDecimal discountPrice) {
        return ApiResponse.<VariantResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.updatePrice(id, price, discountPrice))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteVariant(@PathVariable Long id) {
        return ApiResponse.<MessageResponse>builder()
                .code(SUCCESS.getCode())
                .message(SUCCESS.getMessage())
                .result(variantService.deleteVariant(id))
                .build();
    }

    private CloudinaryUploadResponse uploadVariantImage(MultipartFile file) throws java.io.IOException {
        if (file == null || file.isEmpty()) {
            throw new AppException(StatusCode.FILE_EMPTY);
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", cloudinaryFolder + "/variants",
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
