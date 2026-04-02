package com.buixuantruong.shopapp.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginRequest {
    
    @NotBlank(message = "Access token is required")
    private String accessToken;
    
    private String provider; // "google" or "facebook"
    
    private String redirectUri;
    
    private String code; // for authorization code flow
} 