package ru.stanislav.NauJava3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportPageController {

    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }
}