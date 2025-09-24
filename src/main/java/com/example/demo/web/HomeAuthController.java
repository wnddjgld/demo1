package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class HomeAuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails me, Model model) {
        User user = userRepo.findByUsername(me.getUsername()).orElseThrow();

        // [수정] 역할에 따라 다른 홈으로 리다이렉트
        switch (user.getRole()) {
            case ADMIN:
                return "redirect:/admin/home";
            case PROFESSOR:
                return "redirect:/prof/home";
            case STUDENT:
                // [추가] 교수 신청자인 경우, 승인 대기 페이지로 이동
                if (user.isProfessorApplicant()) {
                    return "redirect:/pending-approval";
                }
                return "redirect:/stu/home";
            default:
                model.addAttribute("me", me);
                return "home";
        }
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register/student")
    public String registerStudent(@ModelAttribute RegForm f) {
        authService.registerStudent(f.getUsername(), f.getPassword(), f.getName());
        return "redirect:/login?registered";
    }

    @PostMapping("/register/prof")
    public String registerProf(@ModelAttribute RegForm f) {
        authService.registerProfessorApplicant(f.getUsername(), f.getPassword(), f.getName());
        return "redirect:/login?prof_applied";
    }
    @GetMapping("/pending-approval")
    public String pendingApprovalPage() {
        return "pending_approval";
    }

    @Data
    public static class RegForm {
        @NotBlank private String username;
        @NotBlank private String password;
        @NotBlank private String name;
    }
}