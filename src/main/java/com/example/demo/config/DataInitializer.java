package com.example.demo.config;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final BCryptPasswordEncoder encoder;

    @Bean
    CommandLineRunner init(UserRepository users) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(encoder.encode("admin1234"))
                        .name("관리자")
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build();
                users.save(admin);
            }
        };
    }
}
