package com.vibebooks.api.controller;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import com.vibebooks.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@RequestBody UserCreateDTO dto){
        User newUser = userService.createUser(dto);

        return new UserResponseDTO(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail()
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        List<User> users = userRepository.findAll();

        List<UserResponseDTO> dtos = users.stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail()))
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
