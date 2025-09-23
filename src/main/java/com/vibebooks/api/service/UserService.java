package com.vibebooks.api.service;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.dto.UserUpdateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserCreateDTO data) {
        if (userRepository.findByUsernameOrEmail(data.username(), data.email()).isPresent()) {
            throw new IllegalStateException("Username or email already registered.");
        }

        String encryptedPassword = passwordEncoder.encode(data.rawPassword());

        User newUser = new User();
        newUser.setUsername(data.username());
        newUser.setEmail(data.email());
        newUser.setPassword(encryptedPassword);

        return userRepository.save(newUser);
    }

    @Transactional
    public User updateUser(UUID id, UserUpdateDTO data, User loggedInUser) {
        if (!loggedInUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to update this profile.");
        }

        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (data.username() != null && !data.username().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.findByUsername(data.username()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use.");
            }
            user.setUsername(data.username());
        }

        if (data.email() != null && !data.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(data.email()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use.");
            }
            user.setEmail(data.email());
        }

        return user;
    }

    @Transactional
    public void deleteUser(UUID id, User loggedInUser) {
        if (!loggedInUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized to delete this profile.");
        }

        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }
}
