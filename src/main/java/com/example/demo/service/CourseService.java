package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional; // [추가]

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Transactional
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

        String savedFileName = null; // [수정] 변수명 변경
        String originalName = null;  // [추가] 원본 파일명을 위한 변수

        if (file != null && !file.isEmpty()) {
            Files.createDirectories(Path.of(uploadDir));

            originalName = file.getOriginalFilename(); // [추가] 원본 파일명 가져오기
            String extension = "";
            if (originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            savedFileName = UUID.randomUUID() + extension; // [수정] UUID + 확장자로 저장
            Path target = Path.of(uploadDir, savedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }

        LocalDateTime now = LocalDateTime.now();
        boolean late = (asg.getDueAt() != null) && now.isAfter(asg.getDueAt());

        Submission s = subRepo.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .orElse(Submission.builder().assignment(asg).student(student).build());

        s.setText(text);

        // [수정] 파일 경로와 원본 파일명 저장 로직
        if (savedFileName != null) {
            s.setFilePath(savedFileName);
            s.setOriginalFileName(originalName);
        }

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
    public void deleteCourse(Long courseId) {
        // 1. 해당 수업의 과제들에 속한 모든 제출물 삭제
        List<Assignment> assignments = asgRepo.findByCourseIdOrderByCreatedAtDesc(courseId);
        for (Assignment assignment : assignments) {
            subRepo.deleteAllByAssignment_Id(assignment.getId());
        }

        // 2. 수업에 직접적으로 관련된 공지, 과제, 수강신청 정보 삭제
        annRepo.deleteAllByCourse_Id(courseId);
        asgRepo.deleteAllByCourse_Id(courseId);
        enrollRepo.deleteAllByCourse_Id(courseId);

        // 3. 최종적으로 수업 자체를 삭제
        courseRepo.deleteById(courseId);
    }

    /**
     * [추가] 과제 삭제 로직
     * @param assignmentId 삭제할 과제 ID
     */
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        // 1. 과제에 속한 모든 제출물 삭제
        subRepo.deleteAllByAssignment_Id(assignmentId);
        // 2. 과제 자체를 삭제
        asgRepo.deleteById(assignmentId);
    }
}
