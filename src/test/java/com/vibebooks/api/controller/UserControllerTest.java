package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should create a new user successfully and return status 201")
    void shouldCreateUserSuccessfully() throws Exception {
        var userDTO = new UserCreateDTO("newUserTest", "test@email.com", "password123");
        var userJson = objectMapper.writeValueAsString(userDTO);

        var response = mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        );

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newUserTest"))
                .andExpect(jsonPath("$.email").value("test@email.com"));

        User savedUser = userRepository.findByEmail("test@email.com")
                .orElseThrow(() -> new IllegalStateException("User not found in database"));

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newUserTest");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Should return status 409 Conflict when creating a user with an existing email")
    void shouldReturnConflictWhenCreatingUserWithExistingEmail() throws Exception {
        // ARRANGE (Arrumar a cena)
        // 1. Primeiro, criamos um usuário "pré-existente" diretamente no banco de dados de teste.
        var existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setEmail("existing@email.com");
        existingUser.setPassword("any_password");
        userRepository.save(existingUser);

        // 2. Agora, criamos um DTO com o MESMO email, tentando criar um usuário duplicado.
        var duplicateUserDTO = new UserCreateDTO("newUser", "existing@email.com", "password123");
        var jsonRequest = objectMapper.writeValueAsString(duplicateUserDTO);


        // ACT (Executar a ação)
        var response = mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        );

        // ASSERT (Verificar o resultado)
        // A única coisa que nos importa é verificar se o status da resposta é 409 Conflict.
        response.andExpect(status().isConflict());
    }
}