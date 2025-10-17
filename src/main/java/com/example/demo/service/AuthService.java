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
                .role(Role.STUDENT).enabled(true)
                .professorApplicant(false) // [수정] 일반 학생은 false
                .build();
        return users.save(u);
    }

    public User registerProfessorApplicant(String username, String rawPw, String name) {
        // 처음엔 STUDENT 역할이지만, 교수 신청자임을 명시
        User u = User.builder()
                .username(username).password(encoder.encode(rawPw)).name(name)
                .role(Role.STUDENT).enabled(true) // 역할은 학생으로 유지
                .professorApplicant(true)      // [수정] 교수 신청자는 true
                .build();
        return users.save(u);
    }

    public User approveProfessor(Long userId) {
        User u = users.findById(userId).orElseThrow();
        u.setRole(Role.PROFESSOR);
        u.setProfessorApplicant(false); // [추가] 승인 후 상태 변경
        return users.save(u);
    }
    /**
     * [추가] 관리자가 사용자의 상태를 변경하는 메서드
     * @param userId 사용자 ID
     * @param status 변경할 상태 ("STUDENT", "PROFESSOR", "PENDING")
     */
    public User updateUserStatus(Long userId, String status) {
        User u = users.findById(userId).orElseThrow();

        switch (status) {
            case "STUDENT":
                u.setRole(Role.STUDENT);
                u.setProfessorApplicant(false);
                break;
            case "PROFESSOR":
                u.setRole(Role.PROFESSOR);
                u.setProfessorApplicant(false);
                break;
            case "PENDING":
                u.setRole(Role.STUDENT);
                u.setProfessorApplicant(true);
                break;
        }
        return users.save(u);
    }
}
