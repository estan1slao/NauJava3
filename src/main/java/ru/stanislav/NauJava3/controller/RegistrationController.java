package ru.stanislav.NauJava3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.stanislav.NauJava3.service.RegistrationService;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(
            @RequestParam String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullName,
            @RequestParam String password,
            Model model) {

        try {
            registrationService.register(
                    username, email, fullName, password
            );

            return "redirect:/login?registered";

        } catch (IllegalArgumentException exception) {
            model.addAttribute("message", exception.getMessage());
            return "registration";
        }
    }
}