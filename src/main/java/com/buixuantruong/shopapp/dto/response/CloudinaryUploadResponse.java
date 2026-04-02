package com.buixuantruong.shopapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudinaryUploadResponse {
    private String url;
    private String publicId;
    private String originalFilename;
}
