package com.buixuantruong.shopapp.controller;

import com.buixuantruong.shopapp.model.SocialLoginRequest;
import com.buixuantruong.shopapp.model.SocialLoginResponse;
import com.buixuantruong.shopapp.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialLoginService socialLoginService;

    @PostMapping("/google")
    public ResponseEntity<SocialLoginResponse> loginWithGoogle(@RequestBody SocialLoginRequest request) {
        try {
            SocialLoginResponse response = socialLoginService.loginWithGoogle(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SocialLoginResponse.builder()
                            .success(false)
                            .message("Google authentication failed: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/facebook")
    public ResponseEntity<SocialLoginResponse> loginWithFacebook(@RequestBody SocialLoginRequest request) {
        try {
            SocialLoginResponse response = socialLoginService.loginWithFacebook(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SocialLoginResponse.builder()
                            .success(false)
                            .message("Facebook authentication failed: " + e.getMessage())
                            .build()
            );
        }
    }
} 