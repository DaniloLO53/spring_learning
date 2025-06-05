package org.example.project.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.project.exceptions.APIException;
import org.example.project.models.SocialUser;
import org.example.project.payload.UserDTO;
import org.example.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public UserDTO createUser(UserDTO userDTO) {
        SocialUser user = modelMapper.map(userDTO, SocialUser.class);
        SocialUser existingUser = userRepository.findByName(user.getName());

        if (existingUser != null) {
            throw new APIException("User with name " + user.getName() + " already exists");
        }

        SocialUser savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }
}
