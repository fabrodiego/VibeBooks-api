package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.UserPasswordUpdateDTO;
import com.vibebooks.api.dto.UserUpdateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the UserController.
 * Verifies HTTP endpoints for user management (GET, PUT, DELETE) and ensures security rules apply.
 */
class UserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;
    private String validJwtToken;

    /**
     * Prepares the database and generates a real JWT token before each test.
     */
    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("integration_user");
        user.setEmail("integration@email.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setBio("Original bio");
        savedUser = userRepository.save(user);

        AuthenticationDTO loginDTO = new AuthenticationDTO("integration@email.com", "password123");
        String responseContent = mockMvc.perform(post("/vibebooks/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);
        validJwtToken = jsonNode.get("token").asText();
    }

    /**
     * Tests the retrieval of all users.
     * Expects a 200 OK and a list containing the user.
     */
    @Test
    @DisplayName("GET /users: Should return a list of users")
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/vibebooks/api/users")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("integration_user"))
                .andExpect(jsonPath("$[0].email").value("integration@email.com"));
    }

    /**
     * Tests updating user data successfully.
     * Expects a 200 OK and the updated fields in the response.
     */
    @Test
    @DisplayName("PUT /users/{id}: Should update user profile successfully")
    void shouldUpdateUserProfile() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO("updated_user", "updated@email.com", "Updated bio");

        mockMvc.perform(put("/vibebooks/api/users/" + savedUser.getId())
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated_user"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    /**
     * Tests the security rule where a user cannot update another user's profile.
     * Expects a 403 Forbidden.
     */
    @Test
    @DisplayName("PUT /users/{id}: Should return 403 Forbidden when updating another user")
    void shouldDenyUpdatingAnotherUser() throws Exception {
        User victimUser = new User();
        victimUser.setUsername("victim");
        victimUser.setEmail("victim@email.com");
        victimUser.setPassword(passwordEncoder.encode("pass"));
        victimUser = userRepository.save(victimUser);

        UserUpdateDTO updateDTO = new UserUpdateDTO("hacked", "hacked@email.com", "hacked");

        mockMvc.perform(put("/vibebooks/api/users/" + victimUser.getId())
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests changing the password successfully.
     * Expects a 200 OK.
     */
    @Test
    @DisplayName("PUT /users/{id}/password: Should change password successfully")
    void shouldChangePassword() throws Exception {
        UserPasswordUpdateDTO pwdDTO = new UserPasswordUpdateDTO("password123", "new_secure_password");

        mockMvc.perform(put("/vibebooks/api/users/" + savedUser.getId() + "/password")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwdDTO)))
                .andExpect(status().isOk());
    }

    /**
     * Tests user deletion.
     * Expects a 204 No Content, and verifies the user is removed from the database.
     */
    @Test
    @DisplayName("DELETE /users/{id}: Should delete user account successfully")
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/vibebooks/api/users/" + savedUser.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent()); // Adjusted to match ResponseEntity.noContent().build()

        boolean exists = userRepository.existsById(savedUser.getId());
        assertThat(exists).isFalse();
    }
}