package com.buixuantruong.shopapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    @NotBlank(message = "title is required")
    @Size(min = 3, max = 200, message = "title must be at least 3 characters")
    private String name;

    private String description;

    private Long categoryId;

//    @Valid
//    private List<VariantDTO> variants;
}
