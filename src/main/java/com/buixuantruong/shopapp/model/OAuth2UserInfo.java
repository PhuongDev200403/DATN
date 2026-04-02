package com.buixuantruong.shopapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2UserInfo {
    private Map<String, Object> attributes;
    private String id;
    private String name;
    private String email;
    private String imageUrl;
    private OAuth2Provider provider;
    
    public static OAuth2UserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .attributes(attributes)
                .id((String) attributes.get("sub"))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .imageUrl((String) attributes.get("picture"))
                .provider(OAuth2Provider.GOOGLE)
                .build();
    }
    
    public static OAuth2UserInfo extractFacebookUserInfo(Map<String, Object> attributes) {
        String imageUrl = null;
        if (attributes.containsKey("picture")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
            if (pictureObj.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                if (dataObj.containsKey("url")) {
                    imageUrl = (String) dataObj.get("url");
                }
            }
        }
        
        return OAuth2UserInfo.builder()
                .attributes(attributes)
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .imageUrl(imageUrl)
                .provider(OAuth2Provider.FACEBOOK)
                .build();
    }
} 