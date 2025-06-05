package org.example.project.contollers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.project.constants.AppConstants;
import org.example.project.payload.UserDTO;
import org.example.project.payload.UserResponse;
import org.example.project.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Data
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<UserResponse> getAllUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
            ) {

        return ResponseEntity.ok(userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }
}
