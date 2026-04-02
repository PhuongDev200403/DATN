package com.buixuantruong.shopapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname", length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 10, nullable = false)
    private String phoneNumber;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "active")
    private boolean active;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialAccount> socialAccounts;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    // Add a transient field to cache authorities
    @Transient
    private Collection<SimpleGrantedAuthority> cachedAuthorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (cachedAuthorities != null) {
            return cachedAuthorities;
        }
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        Role effectiveRole = getRole() != null ? getRole() : Role.USER;
        authorityList.add(new SimpleGrantedAuthority("ROLE_" + effectiveRole.name()));
        this.cachedAuthorities = authorityList;
        return authorityList;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // Helper methods for social login
    public boolean hasSocialAccount(String provider) {
        if (socialAccounts == null || socialAccounts.isEmpty()) {
            return false;
        }
        return socialAccounts.stream()
                .anyMatch(account -> account.getProvider().equals(provider));
    }
    
    public SocialAccount getSocialAccount(String provider) {
        if (socialAccounts == null || socialAccounts.isEmpty()) {
            return null;
        }
        return socialAccounts.stream()
                .filter(account -> account.getProvider().equals(provider))
                .findFirst()
                .orElse(null);
    }
    
    public void addSocialAccount(SocialAccount socialAccount) {
        if (socialAccounts == null) {
            socialAccounts = new ArrayList<>();
        }
        socialAccounts.add(socialAccount);
        socialAccount.setUser(this);
    }
}
