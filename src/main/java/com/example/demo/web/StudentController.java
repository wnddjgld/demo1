package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stu")
public class StudentController {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository asgRepo;
    private final CourseService courseService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseRepo.findAll());
        return "stu/courses";
    }

    @PostMapping("/join/{courseId}")
    public String join(@AuthenticationPrincipal UserDetails me, @PathVariable Long courseId) {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.joinCourse(student, courseId);
        return "redirect:/stu/courses";
    }

    @GetMapping("/assign/{assignmentId}")
    public String assignment(@PathVariable Long assignmentId, Model model) {
        model.addAttribute("asg", asgRepo.findById(assignmentId).orElseThrow());
        return "stu/assignment";
    }

    @PostMapping("/submit/{assignmentId}")
    public String submit(@AuthenticationPrincipal UserDetails me,
                         @PathVariable Long assignmentId,
                         @RequestParam(required = false) String text,
                         @RequestParam(required = false) MultipartFile file) throws Exception {
        User student = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.submitAssignment(student, assignmentId, text, file, uploadDir);
        return "redirect:/stu/assign/" + assignmentId;
    }
}
