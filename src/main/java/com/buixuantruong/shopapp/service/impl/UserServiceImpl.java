package com.buixuantruong.shopapp.service.impl;

import com.buixuantruong.shopapp.dto.UpdateUserDTO;
import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.dto.response.UserResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.model.SocialAccount;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.SocialAccountRepository;
import com.buixuantruong.shopapp.repository.UserRepository;
import com.buixuantruong.shopapp.security.jwt.JWTTokenUtil;
import com.buixuantruong.shopapp.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    SocialAccountRepository socialAccountRepository;
    PasswordEncoder passwordEncoder;
    JWTTokenUtil jwtTokenUtil;
    AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponse createUser(UserDTO userDTO) {
        String phoneNumber = userDTO.getPhoneNumber();
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AppException(StatusCode.USER_EXISTED);
        }
        Role role = userDTO.getRole();
        if (role == null) {
            throw new AppException(StatusCode.ROLE_NOT_FOUND);
        }
        if (role == Role.ADMIN) {
            throw new AppException(StatusCode.ADMIN_ROLE_NOT_ALLOWED);
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
        if (!isSocialLogin) {
            newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
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

        return UserResponse.fromUser(userRepository.save(newUser));
    }

    @Override
    public String login(String phoneNumber, String password) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty()) {
            throw new AppException(StatusCode.INVALID_CREDENTIALS);
        }
        User existingUser = optionalUser.get();

        boolean isSocialUser = existingUser.getSocialAccounts() != null && !existingUser.getSocialAccounts().isEmpty();
        if (!isSocialUser && !passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new AppException(StatusCode.INVALID_CREDENTIALS);
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phoneNumber, password, existingUser.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    @Override
    public User getUserDetailByToken(String token) {
        if (jwtTokenUtil.isTokenExpired(token)) {
            throw new AppException(StatusCode.INVALID_TOKEN);
        }
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
    }

    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (newPhoneNumber != null && !existingUser.getPhoneNumber().equals(newPhoneNumber) &&
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new AppException(StatusCode.USER_EXISTED);
        }

        if (updatedUserDTO.getFullName() != null) existingUser.setFullName(updatedUserDTO.getFullName());
        if (newPhoneNumber != null) existingUser.setPhoneNumber(newPhoneNumber);
        if (updatedUserDTO.getAddress() != null) existingUser.setAddress(updatedUserDTO.getAddress());
        if (updatedUserDTO.getDateOfBirth() != null) existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        if (updatedUserDTO.getEmail() != null) existingUser.setEmail(updatedUserDTO.getEmail());
        if (updatedUserDTO.getAvatarUrl() != null) existingUser.setAvatarUrl(updatedUserDTO.getAvatarUrl());

        if (updatedUserDTO.getSocialProvider() != null && updatedUserDTO.getSocialProviderId() != null) {
            SocialAccount existingSocialAccount = existingUser.getSocialAccount(updatedUserDTO.getSocialProvider());
            if (existingSocialAccount != null) {
                existingSocialAccount.setProviderId(updatedUserDTO.getSocialProviderId());
                if (updatedUserDTO.getEmail() != null) existingSocialAccount.setEmail(updatedUserDTO.getEmail());
                if (updatedUserDTO.getFullName() != null) existingSocialAccount.setName(updatedUserDTO.getFullName());
                if (updatedUserDTO.getAvatarUrl() != null) existingSocialAccount.setPictureUrl(updatedUserDTO.getAvatarUrl());
            } else {
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

        if (updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()) {
            if (!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
                throw new AppException(StatusCode.PASSWORD_NOT_MATCH);
            }
            existingUser.setPassword(passwordEncoder.encode(updatedUserDTO.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public List<UserDTO> getAllUsers() throws Exception {
        return null;
    }
}
