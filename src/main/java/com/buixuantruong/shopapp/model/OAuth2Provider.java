package com.buixuantruong.shopapp.model;

import com.buixuantruong.shopapp.exception.AppException;
import com.buixuantruong.shopapp.exception.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OAuth2Provider {
    GOOGLE("google"),
    FACEBOOK("facebook");

    private String providerName;

    public static OAuth2Provider fromString(String provider) {
        for (OAuth2Provider oauth2Provider : OAuth2Provider.values()) {
            if (oauth2Provider.getProviderName().equalsIgnoreCase(provider)) {
                return oauth2Provider;
            }
        }
        throw new AppException(StatusCode.SOCIAL_PROVIDER_INVALID);
    }
}
