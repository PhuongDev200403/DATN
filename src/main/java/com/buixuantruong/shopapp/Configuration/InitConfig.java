package com.buixuantruong.shopapp.Configuration;

import com.buixuantruong.shopapp.model.Role;
import com.buixuantruong.shopapp.model.User;
import com.buixuantruong.shopapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class InitConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByPhoneNumber("0327391502").isEmpty()) {
            User admin = User.builder()
                    .fullName("admin")
                    .email("admin@gmail.com")
                    .phoneNumber("0327391502")
                    .password(passwordEncoder.encode("admin"))
                    .active(true)
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin account created: username=admin, password=admin");
        }
    }
}
