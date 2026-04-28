package com.project.rentixoficial;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Adăugăm /api pentru a fi diferit de rutele React
public class ContentController {

    @GetMapping("/home")
    public String handleWelcome(){
        return "Salut! Acesta este mesajul de la server pentru Home";
    }

    @GetMapping("/admin/home")
    public String handleAdminHome(){
        return "Date protejate pentru Admin";
    }

    @GetMapping("/user/home")
    public String handleUserHome(){
        return "Date protejate pentru User";
    }
}
