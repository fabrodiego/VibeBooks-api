package com.vibebooks.api.service;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
