package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.UpdateUserDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.exception.DataNotFoundException;
import com.buixuantruong.shopapp.exception.ExpiredTokenException;
import com.buixuantruong.shopapp.exception.PermissionException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.model.SocialAccount;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.SocialAccountRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.security.jwt.JWTTokenUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements com.buixuantruong.shopapp.service.UserService {
    UserRepository userRepository;
    SocialAccountRepository socialAccountRepository;
    PasswordEncoder passwordEncoder;
    JWTTokenUtil jwtTokenUtil;
    AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public ApiResponse<Object> createUser(UserDTO userDTO) throws Exception{
        String phoneNumber = userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role = userDTO.getRole();
        if (role == null) {
            throw new DataNotFoundException("Role not found");
        }
        if (role == Role.ADMIN) {
            throw new PermissionException("Cannot create user with admin role");
        }
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .email(userDTO.getEmail())
                .avatarUrl(userDTO.getAvatarUrl())
                .socialAccounts(new ArrayList<>())
                .build();
        newUser.setRole(role);

        boolean isSocialLogin = userDTO.getSocialProvider() != null && userDTO.getSocialProviderId() != null;
        
        if(!isSocialLogin) {
            // Người dùng thông thường, mã hóa mật khẩu
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        } else {
            // Người dùng đăng nhập xã hội
            SocialAccount socialAccount = SocialAccount.builder()
                    .provider(userDTO.getSocialProvider())
                    .providerId(userDTO.getSocialProviderId())
                    .email(userDTO.getEmail())
                    .name(userDTO.getFullName())
                    .pictureUrl(userDTO.getAvatarUrl())
                    .build();
            socialAccount.setUser(newUser);
            newUser.getSocialAccounts().add(socialAccount);
        }
        
        User savedUser = userRepository.save(newUser);
        
        return ApiResponse.builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(savedUser)
                .build();
    }

    @Override
    public String login(String phoneNumber, String password) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("Invalid phone number or password");
        }
        User existingUser = optionalUser.get();

        // Kiểm tra nếu đây là tài khoản thông thường (không phải social login)
        boolean isSocialUser = existingUser.getSocialAccounts() != null && !existingUser.getSocialAccounts().isEmpty();
        
        if(!isSocialUser){
            if(!passwordEncoder.matches(password, existingUser.getPassword())){
                throw new DataNotFoundException("Invalid phone number or password");
            }
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phoneNumber, password, existingUser.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailByToken(String token) throws Exception {
        if (jwtTokenUtil.isTokenExpired(token)) {
            throw new ExpiredTokenException("Token is expired");
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception("User not found");
        }
    }

    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (newPhoneNumber != null && !existingUser.getPhoneNumber().equals(newPhoneNumber) &&
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        if (updatedUserDTO.getFullName() != null) {
            existingUser.setFullName(updatedUserDTO.getFullName());
        }
        if (newPhoneNumber != null) {
            existingUser.setPhoneNumber(newPhoneNumber);
        }
        if (updatedUserDTO.getAddress() != null) {
            existingUser.setAddress(updatedUserDTO.getAddress());
        }
        if (updatedUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        }
        if (updatedUserDTO.getEmail() != null) {
            existingUser.setEmail(updatedUserDTO.getEmail());
        }
        if (updatedUserDTO.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(updatedUserDTO.getAvatarUrl());
        }
        
        // Xử lý cập nhật tài khoản xã hội nếu cần
        if (updatedUserDTO.getSocialProvider() != null && updatedUserDTO.getSocialProviderId() != null) {
            // Tìm tài khoản xã hội hiện có với provider tương tự
            SocialAccount existingSocialAccount = existingUser.getSocialAccount(updatedUserDTO.getSocialProvider());
            
            if (existingSocialAccount != null) {
                // Cập nhật tài khoản xã hội hiện có
                existingSocialAccount.setProviderId(updatedUserDTO.getSocialProviderId());
                if (updatedUserDTO.getEmail() != null) {
                    existingSocialAccount.setEmail(updatedUserDTO.getEmail());
                }
                if (updatedUserDTO.getFullName() != null) {
                    existingSocialAccount.setName(updatedUserDTO.getFullName());
                }
                if (updatedUserDTO.getAvatarUrl() != null) {
                    existingSocialAccount.setPictureUrl(updatedUserDTO.getAvatarUrl());
                }
            } else {
                // Tạo tài khoản xã hội mới
                SocialAccount newSocialAccount = SocialAccount.builder()
                        .provider(updatedUserDTO.getSocialProvider())
                        .providerId(updatedUserDTO.getSocialProviderId())
                        .email(updatedUserDTO.getEmail())
                        .name(updatedUserDTO.getFullName())
                        .pictureUrl(updatedUserDTO.getAvatarUrl())
                        .user(existingUser)
                        .build();
                
                if (existingUser.getSocialAccounts() == null) {
                    existingUser.setSocialAccounts(new ArrayList<>());
                }
                existingUser.getSocialAccounts().add(newSocialAccount);
            }
        }

        // Cập nhật mật khẩu nếu được cung cấp
        if (updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()) {
            if (!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
                throw new DataNotFoundException("Password and retype password not the same");
            }
            String newPassword = updatedUserDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodedPassword);
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        userRepository.delete(user);
    }

    @Override
    public List<UserDTO> getAllUsers() throws Exception {
//        return userRepository.findAll().stream()
//                .map((element) -> modelMapper.map(element, UserResponse.class)).toList();
        return null;
    }
}
