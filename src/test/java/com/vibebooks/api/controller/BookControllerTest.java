package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import com.vibebooks.api.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Test to verify that creating a book without an authentication token
     * results in a 403 Forbidden status.
     * @throws Exception if an error occurs during the mock MVC perform operation.
     */
    @Test
    @DisplayName("Should return 403 Forbidden when trying to create a book without a token")
    void createBook_WithoutToken_ShouldReturn403() throws Exception {

        var bookDTO = new BookCreationDTO("New Test Book", "Test Author", "1234567890", 2025, null);
        var jsonRequest = objectMapper.writeValueAsString(bookDTO);

        mockMvc.perform(
                        post("/vibebooks/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Test to verify that creating a book with a valid authentication token
     * results in a 201 Created status and the book's title is correctly returned.
     * @throws Exception if an error occurs during the mock MVC perform operation.
     */
    @Test
    @DisplayName("Should return 201 Created when creating a book with a valid token")
    void createBook_WithValidToken_ShouldReturn201() throws Exception {
        var testUser = new User();
        testUser.setUsername("bookTester");
        testUser.setEmail("booktester@email.com");
        testUser.setPassword("hashed_password");
        userRepository.save(testUser);

        var authToken = tokenService.generateToken(testUser);

        var bookDTO = new BookCreationDTO("Another Test Book", "Test Author", "0987654321", 2025, null);
        var jsonRequest = objectMapper.writeValueAsString(bookDTO);

        mockMvc.perform(
                        post("/vibebooks/api/books")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Another Test Book"));
    }
}