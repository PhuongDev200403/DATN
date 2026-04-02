package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.model.OAuth2UserInfo;
import com.buixuantruong.shopapp.model.SocialLoginRequest;
import com.buixuantruong.shopapp.model.SocialLoginResponse;

public interface SocialLoginService {
    SocialLoginResponse loginWithGoogle(SocialLoginRequest request) throws Exception;
    SocialLoginResponse loginWithFacebook(SocialLoginRequest request) throws Exception;
    OAuth2UserInfo getGoogleUserInfo(String accessToken) throws Exception;
    OAuth2UserInfo getFacebookUserInfo(String accessToken) throws Exception;
} 