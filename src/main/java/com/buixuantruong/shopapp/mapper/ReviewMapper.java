package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.ReviewDTO;
import com.buixuantruong.shopapp.dto.response.ReviewResponse;
import com.buixuantruong.shopapp.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "adminResponse", ignore = true)
    Review toReview(ReviewDTO dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "createdAt", source = "createAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ReviewResponse toResponse(Review review);
}
