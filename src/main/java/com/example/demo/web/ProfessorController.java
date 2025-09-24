package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.domain.Course;
import com.example.demo.domain.Assignment;
import com.example.demo.domain.Submission;
import com.example.demo.repository.*;
import com.example.demo.service.CourseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/prof")
public class ProfessorController {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final SubmissionRepository submissionRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final CourseService courseService;

    @GetMapping("/home")
    public String professorHome(@AuthenticationPrincipal UserDetails me, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", prof);

        List<Course> myCourses = courseRepo.findByProfessorId(prof.getId());
        model.addAttribute("courseCount", myCourses.size());

        // 내 과제 수 계산
        long assignmentCount = myCourses.stream()
                .mapToLong(course -> assignmentRepo.findByCourseIdOrderByCreatedAtDesc(course.getId()).size())
                .sum();
        model.addAttribute("assignmentCount", assignmentCount);

        return "prof/home";
    }

    @GetMapping("/courses")
    public String myCourses(@AuthenticationPrincipal UserDetails me, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", prof);
        model.addAttribute("courses", courseRepo.findByProfessorId(prof.getId()));
        return "prof/courses";
    }

    @GetMapping("/create-course")
    public String createCoursePage(@AuthenticationPrincipal UserDetails me, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", prof);
        return "prof/create-course";
    }

    @PostMapping("/courses")
    public String createCourse(@AuthenticationPrincipal UserDetails me, @ModelAttribute CourseForm f) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.createCourse(prof, f.getTitle(), f.getDescription());
        return "redirect:/prof/courses";
    }

    @GetMapping("/course-detail/{courseId}")
    public String courseDetail(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        // 수업 소유권 확인
        if (!course.getProfessor().getId().equals(prof.getId())) {
            return "redirect:/prof/courses";
        }

        model.addAttribute("me", prof);
        model.addAttribute("course", course);
        model.addAttribute("assignments", assignmentRepo.findByCourseIdOrderByCreatedAtDesc(courseId));
        model.addAttribute("enrollments", enrollmentRepo.findByCourseId(courseId));
        return "prof/course-detail";
    }

    @GetMapping("/create-assignment/{courseId}")
    public String createAssignmentPage(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        if (!course.getProfessor().getId().equals(prof.getId())) {
            return "redirect:/prof/courses";
        }

        model.addAttribute("me", prof);
        model.addAttribute("course", course);
        return "prof/create-assignment";
    }

    @PostMapping("/create-assignment/{courseId}")
    public String createAssignment(@PathVariable Long courseId,
                                   @RequestParam String title,
                                   @RequestParam String contentText,
                                   @RequestParam String dueAt) {
        courseService.postAssignment(courseId, title, contentText, LocalDateTime.parse(dueAt));
        return "redirect:/prof/course-detail/" + courseId;
    }

    @GetMapping("/create-announcement/{courseId}")
    public String createAnnouncementPage(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();

        if (!course.getProfessor().getId().equals(prof.getId())) {
            return "redirect:/prof/courses";
        }

        model.addAttribute("me", prof);
        model.addAttribute("course", course);
        return "prof/create-announcement";
    }

    @PostMapping("/create-announcement/{courseId}")
    public String createAnnouncement(@PathVariable Long courseId,
                                     @RequestParam String title,
                                     @RequestParam String content) {
        courseService.postAnnouncement(courseId, title, content);
        return "redirect:/prof/course-detail/" + courseId;
    }

    @GetMapping("/assignment-submissions/{assignmentId}")
    public String assignmentSubmissions(@AuthenticationPrincipal UserDetails me, @PathVariable Long assignmentId, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Assignment assignment = assignmentRepo.findById(assignmentId).orElseThrow();

        if (!assignment.getCourse().getProfessor().getId().equals(prof.getId())) {
            return "redirect:/prof/courses";
        }

        List<Submission> submissions = courseService.listSubmissions(assignmentId);

        model.addAttribute("me", prof);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        return "prof/assignment-submissions";
    }

    @GetMapping("/grade-submission/{submissionId}")
    public String gradeSubmissionPage(@AuthenticationPrincipal UserDetails me, @PathVariable Long submissionId, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        Submission submission = submissionRepo.findById(submissionId).orElseThrow();

        if (!submission.getAssignment().getCourse().getProfessor().getId().equals(prof.getId())) {
            return "redirect:/prof/courses";
        }

        model.addAttribute("me", prof);
        model.addAttribute("submission", submission);
        return "prof/grade-submission";
    }

    @PostMapping("/grade-submission/{submissionId}")
    public String gradeSubmission(@PathVariable Long submissionId,
                                  @RequestParam Integer score,
                                  @RequestParam String feedback) {
        Submission submission = submissionRepo.findById(submissionId).orElseThrow();
        courseService.grade(submissionId, score, feedback);
        return "redirect:/prof/assignment-submissions/" + submission.getAssignment().getId();
    }

    @PostMapping("/announce/{courseId}")
    public String postAnnouncement(@PathVariable Long courseId,
                                   @RequestParam String title, @RequestParam String content) {
        courseService.postAnnouncement(courseId, title, content);
        return "redirect:/prof/courses";
    }

    @PostMapping("/assign/{courseId}")
    public String postAssignment(@PathVariable Long courseId,
                                 @RequestParam String title,
                                 @RequestParam String contentText,
                                 @RequestParam String dueAt) {
        courseService.postAssignment(courseId, title, contentText, LocalDateTime.parse(dueAt));
        return "redirect:/prof/courses";
    }

    @Data public static class CourseForm { private String title; private String description; }
}