package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.CommentCreationDTO;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.BookRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the CommentController.
 * Verifies HTTP endpoints for comment management and user interactions.
 */
class CommentControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserBookStatusRepository userBookStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String validJwtToken;
    private Book savedBook;
    private Comment savedComment;

    /**
     * Prepares the database with a user, a token, a book, and a default comment before each test.
     * Respects Foreign Key constraints during cleanup.
     */
    @BeforeEach
    void setup() throws Exception {
        // 1. Cleanup in correct order (children first)
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        userBookStatusRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Create a user
        User savedUser = new User();
        savedUser.setUsername("commenter");
        savedUser.setEmail("commenter@email.com");
        savedUser.setPassword(passwordEncoder.encode("password123"));
        savedUser = userRepository.save(savedUser);

        // 3. Perform Login to extract a valid JWT Token
        AuthenticationDTO loginDTO = new AuthenticationDTO("commenter@email.com", "password123");
        String responseContent = mockMvc.perform(post("/vibebooks/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);
        validJwtToken = jsonNode.get("token").asText();

        // 4. Save a default book
        Book book = new Book();
        book.setIsbn("0001112223");
        book.setTitle("Integration Book");
        book.setAuthor("Test Author");
        savedBook = bookRepository.save(book);

        // 5. Save a default comment for DELETE and LIKE tests
        savedComment = new Comment("Initial comment for testing", savedUser, savedBook);
        savedComment = commentRepository.save(savedComment);
    }

    /**
     * Tests the successful creation of a comment.
     */
    @Test
    @DisplayName("POST /comments: Should add comment and return 201 Created")
    void shouldAddCommentSuccessfully() throws Exception {
        CommentCreationDTO dto = new CommentCreationDTO("This is a new test comment", savedBook.getId());

        mockMvc.perform(post("/vibebooks/api/comments")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("This is a new test comment"))
                .andExpect(jsonPath("$.username").value("commenter"));
    }

    /**
     * Tests retrieving a paginated list of comments for a specific book.
     */
    @Test
    @DisplayName("GET /comments: Should return paginated list of comments by book")
    void shouldListCommentsByBook() throws Exception {
        mockMvc.perform(get("/vibebooks/api/comments")
                        .param("bookId", savedBook.getId().toString())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value("Initial comment for testing"));
    }

    /**
     * Tests the comment deletion endpoint.
     */
    @Test
    @DisplayName("DELETE /comments/{id}: Should delete comment and return 204 No Content")
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/vibebooks/api/comments/" + savedComment.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());

        boolean exists = commentRepository.existsById(savedComment.getId());
        assertThat(exists).isFalse();
    }

    /**
     * Tests the like toggle endpoint on a comment.
     */
    @Test
    @DisplayName("POST /comments/{id}/like: Should toggle like status on comment")
    void shouldToggleLikeComment() throws Exception {
        mockMvc.perform(post("/vibebooks/api/comments/" + savedComment.getId() + "/like")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.likesCount").value(1));
    }
}