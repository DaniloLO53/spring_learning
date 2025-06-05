package org.example.project.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.project.exceptions.APIException;
import org.example.project.models.SocialUser;
import org.example.project.payload.UserDTO;
import org.example.project.payload.UserResponse;
import org.example.project.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Data
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private ModelMapper modelMapper;

    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sortByAndOrder = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<SocialUser> dbUsersPage = userRepository.findAll(pageDetails);

        List<UserDTO> usersDTO = dbUsersPage.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        UserResponse userResponse = new UserResponse();
        userResponse.setContent(usersDTO);
        userResponse.setPageNumber(dbUsersPage.getNumber());
        userResponse.setPageSize(dbUsersPage.getSize());
        userResponse.setTotalElements(dbUsersPage.getTotalElements());
        userResponse.setTotalPages(dbUsersPage.getTotalPages());
        userResponse.setLastPage(dbUsersPage.isLast());

        return userResponse;
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
