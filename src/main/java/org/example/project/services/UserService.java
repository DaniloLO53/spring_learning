package org.example.project.services;

import org.example.project.models.SocialUser;
import org.example.project.payloads.UserDTO;
import org.example.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<SocialUser> getAllUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<UserDTO> createUser(UserDTO userDTO) {
        SocialUser user = modelMapper.map(userDTO, SocialUser.class);
        UserDTO savedUserDTO = modelMapper.map((userRepository.save(user)), UserDTO.class);
        return new ResponseEntity<>(savedUserDTO, HttpStatus.CREATED);
    }

    public ResponseEntity<UserDTO> updateUser(Long id, UserDTO userDTO) {
        Optional<SocialUser> optionalSavedUser = userRepository.findById(id);

        if (optionalSavedUser.isPresent()) {
            SocialUser savedUser = optionalSavedUser.get();
            savedUser.setName(userDTO.getName());

            return new ResponseEntity<>(modelMapper.map(savedUser, UserDTO.class), HttpStatus.CREATED);
        }
        throw new RuntimeException("User with id " + id + " not found");
    }

    public ResponseEntity<UserDTO> deleteUser(Long id) {
        Optional<SocialUser> optionalSocialUser = userRepository.findById(id);

        if (optionalSocialUser.isPresent()) {
            userRepository.deleteById(id);
            return new ResponseEntity<>(modelMapper.map(optionalSocialUser.get(), UserDTO.class), HttpStatus.CREATED);
        }
        throw new RuntimeException("User with id " + id + " not found");
    }
}
