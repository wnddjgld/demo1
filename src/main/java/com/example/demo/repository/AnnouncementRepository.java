package com.example.demo.repository;

import com.example.demo.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    @Transactional
    void deleteAllByCourse_Id(Long courseId);
}
