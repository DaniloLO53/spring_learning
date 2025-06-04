package org.example.project.controllers;

import org.example.project.models.SocialUser;
import org.example.project.services.UserService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/users")
    public SocialUser createUser(@RequestBody SocialUser user) {
        return userService.createUser(user);
    }

    @PutMapping("/users/{id}")
    public SocialUser updateUser(@PathVariable Long id, @RequestBody SocialUser user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
