package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.UserDTO;
import com.buixuantruong.shopapp.dto.UserLoginDTO;
import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.dto.response.CurrentUserResponse;
import com.buixuantruong.shopapp.dto.response.LoginResponse;
import com.buixuantruong.shopapp.dto.response.UserResponse;
import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserDTO userDTO) {
        return ApiResponse.<UserResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(userService.createUser(userDTO))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        String token = userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
        return ApiResponse.<LoginResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(LoginResponse.builder().accessToken(token).build())
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> getCurrentUser(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        User user = resolveCurrentUser(authorizationHeader);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ApiResponse.<CurrentUserResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(CurrentUserResponse.builder()
                        .name(user.getFullName())
                        .authorities(authorities)
                        .authenticated(authentication.isAuthenticated())
                        .build())
                .build();
    }

    private User resolveCurrentUser(String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AppException(StatusCode.UNAUTHENTICATED);
        }

        String token = authorizationHeader.substring(7);
        return userService.getUserDetailByToken(token);
    }
}
