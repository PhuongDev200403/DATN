package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.UpdateUserDTO;
import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.dto.response.UserResponse;
import com.buixuantruong.shopapp.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserResponse createUser(UserDTO userDTO);

    String login(String phoneNumber, String password);

    User getUserDetailByToken(String token);

    User updateUser(Long userId, UpdateUserDTO updatedUserDTO);

    void deleteUser(Long userId);

    List<UserDTO> getAllUsers() throws Exception;
}
