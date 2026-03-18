package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.CommentLikeRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthenticationController.
 * Verifies user registration and login endpoints, including JWT generation.
 */
class AuthenticationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserBookStatusRepository userBookStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        userBookStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Tests successful user registration.
     */
    @Test
    @DisplayName("POST /register: Should register a user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserCreateDTO newUser = new UserCreateDTO("test", "test@email.com", "hardPassword123");

        mockMvc.perform(post("/vibebooks/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    /**
     * Tests validation for duplicate emails during registration.
     */
    @Test
    @DisplayName("POST /register: Should fail when email already exists")
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        // Prepare existing user in the database
        User existingUser = new User();
        existingUser.setUsername("user1");
        existingUser.setEmail("test@email.com");
        existingUser.setPassword(passwordEncoder.encode("123"));
        userRepository.save(existingUser);

        UserCreateDTO duplicateUser = new UserCreateDTO("user2", "test@email.com", "hardPassword123");

        mockMvc.perform(post("/vibebooks/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict());
    }

    /**
     * Tests the login process and JWT token generation.
     */
    @Test
    @DisplayName("POST /login: Should authenticate user and return JWT token")
    void shouldLoginSuccessfully() throws Exception {
        User user = new User();
        user.setUsername("login_user");
        user.setEmail("login@email.com");
        user.setPassword(passwordEncoder.encode("hardPassword123"));
        userRepository.save(user);

        AuthenticationDTO loginDTO = new AuthenticationDTO("login@email.com", "hardPassword123");

        mockMvc.perform(post("/vibebooks/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    /**
     * Tests login failure with incorrect password.
     */
    @Test
    @DisplayName("POST /login: Should fail with bad credentials")
    void shouldFailLoginWithBadCredentials() throws Exception {
        User user = new User();
        user.setUsername("login_user");
        user.setEmail("login@email.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        AuthenticationDTO loginDTO = new AuthenticationDTO("login@email.com", "hardPassword123");

        mockMvc.perform(post("/vibebooks/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().is4xxClientError());
    }
}