package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CourseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/prof")
public class ProfessorController {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final CourseService courseService;

    @GetMapping("/courses")
    public String myCourses(@AuthenticationPrincipal UserDetails me, Model model) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        model.addAttribute("courses", courseRepo.findByProfessorId(prof.getId()));
        return "prof/courses";
    }

    @PostMapping("/courses")
    public String createCourse(@AuthenticationPrincipal UserDetails me, @ModelAttribute CourseForm f) {
        User prof = userRepo.findByUsername(me.getUsername()).orElseThrow();
        courseService.createCourse(prof, f.getTitle(), f.getDescription());
        return "redirect:/prof/courses";
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
