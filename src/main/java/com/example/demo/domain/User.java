package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled;

    // ▼▼▼ 교수 신청자 여부를 나타내는 필드 ▼▼▼
    @Column(columnDefinition = "boolean default false")
    private boolean professorApplicant;
}
