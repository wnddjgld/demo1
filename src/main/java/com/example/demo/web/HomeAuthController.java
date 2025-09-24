package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;
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

    @GetMapping("/")
    public String index() { return "index"; }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails me, Model model) {
        model.addAttribute("me", me);
        return "home";
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

    @Data
    public static class RegForm {
        @NotBlank private String username;
        @NotBlank private String password;
        @NotBlank private String name;
    }
}
