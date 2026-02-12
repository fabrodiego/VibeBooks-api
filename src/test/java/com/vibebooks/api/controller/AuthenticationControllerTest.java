package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso (Caminho Feliz)")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserCreateDTO newUser = new UserCreateDTO(
                "diego_dev",
                "diego@example.com",
                "senhaForte123"
        );

        mockMvc.perform(post("/vibebooks/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("diego_dev"))
                .andExpect(jsonPath("$.email").value("diego@example.com"));
    }

    @Test
    @DisplayName("Deve falhar ao tentar registrar email duplicado")
    void shouldFailWhenEmailAlreadyExists() throws Exception {

        UserCreateDTO user = new UserCreateDTO("user1", "teste@email.com", "123");

        mockMvc.perform(post("/vibebooks/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/vibebooks/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());
    }
}