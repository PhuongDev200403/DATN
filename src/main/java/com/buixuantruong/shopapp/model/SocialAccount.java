package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    @Column(name = "provider_id", length = 100, nullable = false)
    private String providerId;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "picture_url", length = 255)
    private String pictureUrl;
    
    @Column(name = "access_token", length = 255)
    private String accessToken;
    
    @Column(name = "refresh_token", length = 255)
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private Long tokenExpiresAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    public static final String PROVIDER_GOOGLE = "GOOGLE";
    public static final String PROVIDER_FACEBOOK = "FACEBOOK";
}
