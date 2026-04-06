package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.dto.response.ApiResponse;
import com.buixuantruong.shopapp.exception.StatusCode;
import com.buixuantruong.shopapp.model.SocialLoginRequest;
import com.buixuantruong.shopapp.model.SocialLoginResponse;
import com.buixuantruong.shopapp.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialLoginService socialLoginService;

    @PostMapping("/google")
    public ApiResponse<SocialLoginResponse> loginWithGoogle(@RequestBody SocialLoginRequest request) throws Exception {
        return ApiResponse.<SocialLoginResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(socialLoginService.loginWithGoogle(request))
                .build();
    }

    @PostMapping("/facebook")
    public ApiResponse<SocialLoginResponse> loginWithFacebook(@RequestBody SocialLoginRequest request) throws Exception {
        return ApiResponse.<SocialLoginResponse>builder()
                .code(StatusCode.SUCCESS.getCode())
                .message(StatusCode.SUCCESS.getMessage())
                .result(socialLoginService.loginWithFacebook(request))
                .build();
    }
}
