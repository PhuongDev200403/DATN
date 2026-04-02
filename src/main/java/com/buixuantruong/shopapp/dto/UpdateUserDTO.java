package com.buixuantruong.shopapp.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDTO {
    String fullName;
    String phoneNumber;
    String address;
    String password;
    String retypePassword;
    Date dateOfBirth;
    String email;
    String avatarUrl;
    
    // Thông tin đăng nhập xã hội
    String socialProvider; // GOOGLE hoặc FACEBOOK
    String socialProviderId;
}
