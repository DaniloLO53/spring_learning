package org.example.project.services;

import org.example.project.models.SocialUser;
import org.example.project.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<SocialUser> getAllUsers() {
        return userRepository.findAll();
    }
}
