package org.example.project.controllers;

import org.example.project.models.SocialUser;
import org.example.project.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/social")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<SocialUser> getAllUsers() {
        return userService.getAllUsers();
    }


}
