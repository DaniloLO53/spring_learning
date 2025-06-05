package org.example.project.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.project.models.SocialUser;
import org.example.project.payload.UserDTO;
import org.example.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public List<UserDTO> getAllUsers() {
        List<SocialUser> dbUsers = userRepository.findAll();
        List<UserDTO> usersDTO = dbUsers.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        return usersDTO;
    }
}
