package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Variant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse extends BaseResponse {
    Long id;
    String name;
    String description;
    Long categoryId; // ignore
    List<VariantResponse> variants = new ArrayList<>(); //ignore
}
