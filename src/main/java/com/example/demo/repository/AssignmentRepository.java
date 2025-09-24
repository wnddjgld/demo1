package com.example.demo.repository;

import com.example.demo.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    @Transactional
    void deleteAllByCourse_Id(Long courseId);
}
