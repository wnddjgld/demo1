package com.example.demo.web;

import com.example.demo.domain.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepo;
    private final AuthService authService;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("Role", Role.class);
        return "admin/users";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        authService.approveProfessor(id);
        return "redirect:/admin/users";
    }
}
