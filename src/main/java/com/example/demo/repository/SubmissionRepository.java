package com.example.demo.repository;

import com.example.demo.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentId(Long assignmentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    // [추가] 과제 ID로 모든 제출물 삭제
    @Transactional
    void deleteAllByAssignment_Id(Long assignmentId);
}
