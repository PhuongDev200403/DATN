package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.CloudinaryUploadResponse;
import com.cloudinary.Cloudinary;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {

    private static final String CLOUDINARY_HOST = "res.cloudinary.com";

    Cloudinary cloudinary;

    @Value("${cloudinary.folder:shopapp}")
    @NonFinal
    String folder;

    public CloudinaryUploadResponse uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        Map<?, ?> uploadResult = upload(file.getBytes(), folder);

        return CloudinaryUploadResponse.builder()
                .url(String.valueOf(uploadResult.get("secure_url")))
                .publicId(String.valueOf(uploadResult.get("public_id")))
                .originalFilename(file.getOriginalFilename())
                .build();
    }

    public String uploadImage(String imageSource) throws IOException {
        return uploadImage(imageSource, folder);
    }

    public String uploadImage(String imageSource, String targetFolder) throws IOException {
        if (imageSource == null || imageSource.isBlank()) {
            return imageSource;
        }
        if (isCloudinaryUrl(imageSource)) {
            return imageSource;
        }

        Map<?, ?> uploadResult = upload(imageSource, targetFolder);
        return String.valueOf(uploadResult.get("secure_url"));
    }

    private Map<?, ?> upload(Object file, String targetFolder) throws IOException {
        return cloudinary.uploader().upload(
                file,
                Map.of(
                        "folder", targetFolder,
                        "resource_type", "image"
                )
        );
    }

    private boolean isCloudinaryUrl(String imageSource) {
        return imageSource.contains(CLOUDINARY_HOST);
    }
}
