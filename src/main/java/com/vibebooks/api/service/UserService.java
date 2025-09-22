package com.vibebooks.api.service;

import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user in the system, applying business rules.
     * @param username The chosen username.
     * @param email The new user's email.
     * @param rawPassword The password in plain text (unencrypted).
     * @return The User object that was saved in the database.
     * @throws IllegalStateException if the username or email already exist.
     */
    @Transactional
    public User createUser(String username, String email, String rawPassword) {

        // --- BUSINESS RULE 1: CHECK FOR DUPLICATES ---
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalStateException("Username or email already registered.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("The email '" + email + "' is already registered.");
        }

        // --- BUSINESS RULE 2: ENCRYPT THE PASSWORD ---
        String encryptedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(encryptedPassword);

        return userRepository.save(newUser);
    }
}
