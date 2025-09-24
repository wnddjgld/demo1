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
            // ▼▼▼ [추가] 테스트 교수 계정 생성 (승인 완료 상태) ▼▼▼
            if (users.findByUsername("asdf").isEmpty()) {
                users.save(User.builder().username("asdf").password(encoder.encode("asdf"))
                        .name("김교수").role(Role.PROFESSOR).enabled(true).professorApplicant(false).build());
            }

            // ▼▼▼ [추가] 테스트 학생 계정 생성 ▼▼▼
            if (users.findByUsername("123").isEmpty()) {
                users.save(User.builder().username("123").password(encoder.encode("123"))
                        .name("이학생").role(Role.STUDENT).enabled(true).professorApplicant(false).build());
            }
        };
    }
}
