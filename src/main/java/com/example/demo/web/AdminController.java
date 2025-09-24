package com.example.demo.web;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.SubmissionRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final AssignmentRepository assignmentRepo;
    private final SubmissionRepository submissionRepo;
    private final AuthService authService;

    @GetMapping("/home")
    public String adminHome(@AuthenticationPrincipal UserDetails me, Model model) {
        User admin = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", admin);

        // 통계 정보
        long totalUsers = userRepo.count();
        long totalCourses = courseRepo.count();
        long totalAssignments = assignmentRepo.count();
        long totalSubmissions = submissionRepo.count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalAssignments", totalAssignments);
        model.addAttribute("totalSubmissions", totalSubmissions);

        return "admin/home";
    }

    @GetMapping("/users")
    public String users(@AuthenticationPrincipal UserDetails me, Model model) {
        User admin = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", admin);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("Role", Role.class);
        return "admin/users";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        authService.approveProfessor(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String courses(@AuthenticationPrincipal UserDetails me, Model model) {
        User admin = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("me", admin);
        model.addAttribute("courses", courseRepo.findAll());
        return "admin/courses";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {
        // 관리자는 삭제할 수 없음
        User user = userRepo.findById(id).orElseThrow();
        if (!user.getRole().equals(Role.ADMIN)) {
            userRepo.deleteById(id);
        }
        return "redirect:/admin/users";
    }
}