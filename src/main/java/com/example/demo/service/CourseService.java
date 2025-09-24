package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollRepo;
    private final AnnouncementRepository annRepo;
    private final AssignmentRepository asgRepo;
    private final SubmissionRepository subRepo;

    public Course createCourse(User professor, String title, String desc) {
        Course c = Course.builder().title(title).description(desc).professor(professor).build();
        return courseRepo.save(c);
    }

    public void joinCourse(User student, Long courseId) {
        if (!enrollRepo.existsByCourseIdAndStudentId(courseId, student.getId())) {
            Enrollment e = Enrollment.builder()
                    .course(courseRepo.findById(courseId).orElseThrow())
                    .student(student)
                    .joinedAt(LocalDateTime.now())
                    .build();
            enrollRepo.save(e);
        }
    }

    public Announcement postAnnouncement(Long courseId, String title, String content) {
        Announcement a = Announcement.builder()
                .course(courseRepo.findById(courseId).orElseThrow())
                .title(title).content(content).createdAt(LocalDateTime.now()).build();
        return annRepo.save(a);
    }

    public Assignment postAssignment(Long courseId, String title, String contentText, LocalDateTime dueAt) {
        Assignment a = Assignment.builder()
                .course(courseRepo.findById(courseId).orElseThrow())
                .title(title).contentText(contentText)
                .dueAt(dueAt).createdAt(LocalDateTime.now()).build();
        return asgRepo.save(a);
    }

    public Submission submitAssignment(User student, Long assignmentId, String text, MultipartFile file, String uploadDir) throws Exception {
        Assignment asg = asgRepo.findById(assignmentId).orElseThrow();

        String filePath = null;
        if (file != null && !file.isEmpty()) {
            Files.createDirectories(Path.of(uploadDir));
            String saved = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = Path.of(uploadDir, saved);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            filePath = target.toString();
        }

        LocalDateTime now = LocalDateTime.now();
        boolean late = (asg.getDueAt() != null) && now.isAfter(asg.getDueAt());

        Submission s = subRepo.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElse(Submission.builder().assignment(asg).student(student).build());

        s.setText(text);
        s.setFilePath(filePath != null ? filePath : s.getFilePath());
        s.setSubmittedAt(now);
        s.setLate(late);

        return subRepo.save(s);
    }

    public Submission grade(Long submissionId, Integer score, String feedback) {
        Submission s = subRepo.findById(submissionId).orElseThrow();
        s.setScore(score);
        s.setFeedback(feedback);
        return subRepo.save(s);
    }

    public List<Submission> listSubmissions(Long assignmentId) {
        return subRepo.findByAssignmentId(assignmentId);
    }
}
