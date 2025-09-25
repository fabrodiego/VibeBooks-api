package com.vibebooks.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private com.vibebooks.api.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;


    /**
     * Test case to verify that a valid login attempt returns a 200 OK status
     * and a non-empty JWT token.
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    @DisplayName("Should return 200 OK and a JWT token for a valid login")
    void shouldReturnTokenForValidLogin() throws Exception {
        var user = new com.vibebooks.api.model.User();
        user.setUsername("loginUser");
        user.setEmail("login@email.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        var loginDTO = new com.vibebooks.api.dto.AuthenticationDTO("login@email.com", "password123");
        var jsonRequest = objectMapper.writeValueAsString(loginDTO);

        var response = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        );

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token").isNotEmpty());
    }

    /**
     * Test case to verify that an invalid login attempt returns a 403 Forbidden status.
     * This typically happens when the provided credentials do not match any registered user.
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    @DisplayName("Should return 403 Forbidden for an invalid login")
    void shouldReturnForbiddenForInvalidLogin() throws Exception {
        var loginDTO = new com.vibebooks.api.dto.AuthenticationDTO("nonexistent@email.com", "wrongpassword");
        var jsonRequest = objectMapper.writeValueAsString(loginDTO);

        var response = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        );

        response.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
}