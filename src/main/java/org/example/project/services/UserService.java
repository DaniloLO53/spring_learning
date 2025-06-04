package org.example.project.services;

import org.example.project.models.SocialUser;
import org.example.project.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<SocialUser> getAllUsers() {
        return userRepository.findAll();
    }

    public SocialUser createUser(SocialUser user) {
        return userRepository.save(user);
    }

    public SocialUser updateUser(Long id, SocialUser user) {
        Optional<SocialUser> optionalSavedUser = userRepository.findById(id);

        if (optionalSavedUser.isPresent()) {
            SocialUser savedUser = optionalSavedUser.get();
            savedUser.setName(user.getName());
            return savedUser;
        }
        throw new RuntimeException("User with id " + id + " not found");
    }

    public String deleteUser(Long id) {
        Optional<SocialUser> optionalSocialUser = userRepository.findById(id);

        if (optionalSocialUser.isPresent()) {
            userRepository.deleteById(id);
            return "User with id " + id + " has been deleted successfully";
        }
        throw new RuntimeException("User with id " + id + " not found");
    }
}
