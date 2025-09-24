package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) private Assignment assignment;
    @ManyToOne(fetch = FetchType.LAZY) private User student;

    @Lob private String text;
    private String filePath;

    private LocalDateTime submittedAt;
    private boolean late;

    private Integer score;
    @Lob private String feedback;
}
