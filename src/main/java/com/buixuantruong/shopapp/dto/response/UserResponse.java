package com.buixuantruong.shopapp.dto.response;

import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.model.SocialAccount;
import com.buixuantruong.shopapp.model.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String fullName;
    String phoneNumber;
    String email;
    String address;
    Boolean active;
    Date dateOfBirth;
    String avatarUrl;
    List<SocialAccountResponse> socialAccounts;
    Role role;

    @Data
    @Builder
    public static class SocialAccountResponse {
        String provider;
        String providerId;
        String email;
        String name;
        String pictureUrl;
        
        public static SocialAccountResponse fromSocialAccount(SocialAccount socialAccount) {
            return SocialAccountResponse.builder()
                    .provider(socialAccount.getProvider())
                    .providerId(socialAccount.getProviderId())
                    .email(socialAccount.getEmail())
                    .name(socialAccount.getName())
                    .pictureUrl(socialAccount.getPictureUrl())
                    .build();
        }
    }

    public static UserResponse fromUser(User user) {
        List<SocialAccountResponse> socialAccountResponses = null;
        if (user.getSocialAccounts() != null) {
            socialAccountResponses = user.getSocialAccounts().stream()
                    .map(SocialAccountResponse::fromSocialAccount)
                    .collect(Collectors.toList());
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .address(user.getAddress())
                .active(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .socialAccounts(socialAccountResponses)
                .role(user.getRole())
                .build();
    }
}
