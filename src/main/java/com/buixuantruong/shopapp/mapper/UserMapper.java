package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.dto.response.UserResponse;
import com.buixuantruong.shopapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // map từ dto sang entity
    @Mapping(target = "id", ignore = true)
    User toUser(UserDTO dto);

    // map từ entity sang response
    UserResponse toResponse(User user);

}
