package org.example.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security/")
public class GreetingController {
    @GetMapping("/hello")
    public String getGreetings() {
        return "Hello";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/hello/user")
    public String userHello() {
        return "Hello, user!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hello/admin")
    public String adminHello() {
        return "Hello, admin!";
    }
}
