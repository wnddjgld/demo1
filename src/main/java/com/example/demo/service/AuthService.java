package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public User registerStudent(String username, String rawPw, String name) {
        User u = User.builder()
                .username(username).password(encoder.encode(rawPw)).name(name)
                .role(Role.STUDENT).enabled(true).build();
        return users.save(u);
    }

    public User registerProfessorApplicant(String username, String rawPw, String name) {
        // 처음엔 STUDENT로 두고, 관리자 승인 시 ROLE 변경
        User u = User.builder()
                .username(username).password(encoder.encode(rawPw)).name(name)
                .role(Role.STUDENT).enabled(true).build();
        return users.save(u);
    }

    public User approveProfessor(Long userId) {
        User u = users.findById(userId).orElseThrow();
        u.setRole(Role.PROFESSOR);
        return users.save(u);
    }
}
