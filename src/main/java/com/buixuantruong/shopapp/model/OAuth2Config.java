package com.buixuantruong.shopapp.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Config {
    
    private Map<String, ProviderConfig> providers = new HashMap<>();
    
    @Data
    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String scope;
        private String responseType = "code";
        private String grantType = "authorization_code";
    }
} 