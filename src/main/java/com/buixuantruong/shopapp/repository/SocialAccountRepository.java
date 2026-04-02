package com.buixuantruong.shopapp.repository;

import com.buixuantruong.shopapp.model.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(String provider, String providerId);
    boolean existsByProviderAndProviderId(String provider, String providerId);
} 