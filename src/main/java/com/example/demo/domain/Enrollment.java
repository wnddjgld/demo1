package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // [수정] LAZY를 EAGER로 변경
    private Course course;
    @ManyToOne(fetch = FetchType.EAGER)
    private User student;
    private LocalDateTime joinedAt;
}
