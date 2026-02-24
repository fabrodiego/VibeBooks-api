package com.vibebooks.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookIsbnDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.dto.google.GoogleBookItem;
import com.vibebooks.api.dto.google.GoogleBooksResponse;
import com.vibebooks.api.dto.google.ImageLinks;
import com.vibebooks.api.dto.google.VolumeInfo;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.ReadingStatus;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the BookController.
 * Verifies HTTP endpoints for book management, status updates, and external API mocking.
 */
class BookControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserBookStatusRepository userBookStatusRepository;

    /**
     * Replaces the real RestTemplate with a Mockito mock in the Spring Context.
     * This ensures no real HTTP calls are made to Google Books during the test.
     */
    @MockitoBean
    private RestTemplate restTemplate;

    private String validJwtToken;
    private Book savedBook;

    /**
     * Prepares the database with a user, a token, and a default book before each test.
     */
    @BeforeEach
    void setup() throws Exception {
        userBookStatusRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("book_lover");
        user.setEmail("reader@email.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        AuthenticationDTO loginDTO = new AuthenticationDTO("reader@email.com", "password123");
        String responseContent = mockMvc.perform(post("/vibebooks/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);
        validJwtToken = jsonNode.get("token").asText();

        Book book = new Book();
        book.setIsbn("0987654321");
        book.setTitle("Integration Test Book");
        book.setAuthor("Test Author");
        savedBook = bookRepository.save(book);
    }

    /**
     * Tests creating a book by fetching fake data from the mocked Google Books API.
     */
    @Test
    @DisplayName("POST /books: Should create book from Google API and return 201 Created")
    void shouldCreateBook() throws Exception {
        BookIsbnDTO dto = new BookIsbnDTO("1234567890");

        ImageLinks fakeImageLinks = new ImageLinks("http://thumb", "http://small");
        VolumeInfo fakeVolumeInfo = new VolumeInfo("Mocked Book", List.of("Mock Author"), "2024", fakeImageLinks);
        GoogleBookItem fakeItem = new GoogleBookItem(fakeVolumeInfo);
        GoogleBooksResponse fakeResponse = new GoogleBooksResponse(List.of(fakeItem));

        when(restTemplate.getForObject(anyString(), eq(GoogleBooksResponse.class))).thenReturn(fakeResponse);

        mockMvc.perform(post("/vibebooks/api/books")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Mocked Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"));
    }

    /**
     * Tests retrieving a paginated list of books.
     */
    @Test
    @DisplayName("GET /books: Should return paginated list of books")
    void shouldListBooks() throws Exception {
        mockMvc.perform(get("/vibebooks/api/books")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Integration Test Book"));
    }

    /**
     * Tests retrieving a specific book by its ID.
     */
    @Test
    @DisplayName("GET /books/{id}: Should return book details")
    void shouldGetBookById() throws Exception {
        mockMvc.perform(get("/vibebooks/api/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Book"));
    }

    /**
     * Tests updating book details.
     */
    @Test
    @DisplayName("PUT /books/{id}: Should update book details")
    void shouldUpdateBook() throws Exception {
        BookCreationDTO updateDTO = new BookCreationDTO(
                "Updated Title",
                "Updated Author",
                "0987654321",
                2025,
                "http://new-cover.url"
        );

        mockMvc.perform(put("/vibebooks/api/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.author").value("Updated Author"));
    }

    /**
     * Tests the book deletion endpoint.
     */
    @Test
    @DisplayName("DELETE /books/{id}: Should delete book and return 204 No Content")
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(delete("/vibebooks/api/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());

        boolean exists = bookRepository.existsById(savedBook.getId());
        assertThat(exists).isFalse();
    }

    /**
     * Tests the like toggle endpoint.
     */
    @Test
    @DisplayName("POST /books/{id}/like: Should toggle like status")
    void shouldToggleLikeBook() throws Exception {
        mockMvc.perform(post("/vibebooks/api/books/" + savedBook.getId() + "/like")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.totalLikes").value(1));
    }

    /**
     * Tests updating the reading status of a book.
     */
    @Test
    @DisplayName("POST /books/{id}/status: Should update reading status")
    void shouldUpdateBookStatus() throws Exception {
        BookStatusUpdateDTO statusDTO = new BookStatusUpdateDTO(ReadingStatus.READING, null);

        mockMvc.perform(post("/vibebooks/api/books/" + savedBook.getId() + "/status")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReadingStatus.READING.name()));
    }

    /**
     * Tests searching books by title.
     */
    @Test
    @DisplayName("GET /books/search: Should return matched books")
    void shouldSearchBooks() throws Exception {
        mockMvc.perform(get("/vibebooks/api/books/search")
                        .param("query", "Integration")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Integration Test Book"));
    }
}