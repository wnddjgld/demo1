package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.domain.Course;
import com.example.demo.domain.Assignment;
import com.example.demo.domain.Submission;
import com.example.demo.domain.Enrollment;
import com.example.demo.repository.*;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set; // [추가]

@Controller
@RequiredArgsConstructor
@RequestMapping("/stu")
public class StudentController {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository asgRepo;
    private final SubmissionRepository subRepo;
    private final EnrollmentRepository enrollRepo;
    private final CourseService courseService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/home")
    public String studentHome(@AuthenticationPrincipal UserDetails me, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", student);

        List<Enrollment> enrollments = enrollRepo.findByStudentId(student.getId());
        model.addAttribute("courseCount", enrollments.size());

        // 내가 제출한 과제 수 계산
        long submissionCount = subRepo.findByAssignmentId(1L).size(); // 임시로 하드코딩
        model.addAttribute("submissionCount", submissionCount);

        return "stu/home";
    }

    @GetMapping("/courses")
    public String listCourses(@AuthenticationPrincipal UserDetails me, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", student);
        model.addAttribute("courses", courseRepo.findAll());

        // ▼▼▼ [추가] 학생이 이미 수강 중인 수업 ID 목록을 모델에 추가 ▼▼▼
        Set<Long> myCourseIds = enrollRepo.findByStudentId(student.getId())
                .stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());
        model.addAttribute("myCourseIds", myCourseIds);
        // ▲▲▲ 여기까지 ▲▲▲

        return "stu/courses";
    }

    @GetMapping("/my-courses")
    public String myCourses(@AuthenticationPrincipal UserDetails me, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        List<Enrollment> enrollments = enrollRepo.findByStudentId(student.getId());
        List<Course> myCourses = enrollments.stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());

        model.addAttribute("me", student);
        model.addAttribute("courses", myCourses);
        return "stu/my-courses";
    }

    @GetMapping("/course-detail/{courseId}")
    public String courseDetail(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        // 수강 확인
        boolean isEnrolled = enrollRepo.existsByCourseIdAndStudentId(courseId, student.getId());
        if (!isEnrolled) {
            return "redirect:/stu/courses";
        }

        List<Assignment> assignments = asgRepo.findByCourseIdOrderByCreatedAtDesc(courseId);

        model.addAttribute("me", student);
        model.addAttribute("course", course);
        model.addAttribute("assignments", assignments);
        return "stu/course-detail";
    }

    @PostMapping("/join/{courseId}")
    public String join(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.joinCourse(student, courseId);
        return "redirect:/stu/courses";
    }

    @GetMapping("/assignment/{assignmentId}")
    public String assignment(@AuthenticationPrincipal UserDetails me, @PathVariable Long assignmentId, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Assignment assignment = asgRepo.findById(assignmentId).orElseThrow();

        // 수강 확인
        boolean isEnrolled = enrollRepo.existsByCourseIdAndStudentId(assignment.getCourse().getId(), student.getId());
        if (!isEnrolled) {
            return "redirect:/stu/courses";
        }

        // 기존 제출물 확인
        Submission existingSubmission = subRepo.findByAssignmentIdAndStudentId(assignmentId, student.getId()).orElse(null);

        model.addAttribute("me", student);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", existingSubmission);
        return "stu/assignment";
    }

    @PostMapping("/submit/{assignmentId}")
    public String submit(@AuthenticationPrincipal UserDetails me,
                         @PathVariable Long assignmentId,
                         @RequestParam(required = false) String text,
                         @RequestParam(required = false) MultipartFile file) throws Exception {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.submitAssignment(student, assignmentId, text, file, uploadDir);
        return "redirect:/stu/assignment/" + assignmentId;
    }

    @GetMapping("/submission-result/{assignmentId}")
    public String submissionResult(@AuthenticationPrincipal UserDetails me, @PathVariable Long assignmentId, Model model) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Assignment assignment = asgRepo.findById(assignmentId).orElseThrow();
        Submission submission = subRepo.findByAssignmentIdAndStudentId(assignmentId, student.getId()).orElse(null);

        if (submission == null) {
            return "redirect:/stu/assignment/" + assignmentId;
        }

        model.addAttribute("me", student);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", submission);
        return "stu/submission-result";
    }
}