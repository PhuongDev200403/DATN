package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.UpdateUserDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface  UserService {
    ApiResponse<Object> createUser(UserDTO userDTO) throws Exception;

    String login(String phoneNumber, String password) throws DataNotFoundException;

    User getUserDetailByToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;
    void deleteUser(Long userId) throws Exception;

    List<UserDTO> getAllUsers() throws Exception;
}
