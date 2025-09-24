package com.example.demo.repository;

import com.example.demo.domain.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
