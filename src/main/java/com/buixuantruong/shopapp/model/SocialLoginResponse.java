package com.buixuantruong.shopapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String message;
    private boolean success;
    private UserProfile userProfile;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserProfile {
        private Long id;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String avatarUrl;
        private String role;
        private boolean isNewUser;
    }
} 