package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // [수정] LAZY를 EAGER로 변경
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.EAGER) // [수정] LAZY를 EAGER로 변경
    private User student;

    @Lob private String text;
    private String filePath;

    private String originalFileName;

    private LocalDateTime submittedAt;
    private boolean late;

    private Integer score;
    @Lob private String feedback;



}
