package com.vibebooks.api.controller;

import com.vibebooks.api.AbstractIntegrationTest;
import com.vibebooks.api.model.*;
import com.vibebooks.api.repository.*;
import com.vibebooks.api.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link FeedController}.
 * Verifies the feed endpoint, ensuring that batch data aggregation
 * is correctly serialized into the JSON response.
 */
class FeedControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private TokenService tokenService;

    private String validJwtToken;

    @BeforeEach
    void setup() {
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        userBookStatusRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        User owner = new User();
        owner.setUsername("feed_user");
        owner.setEmail("feed@email.com");
        owner.setPassword(passwordEncoder.encode("password123"));
        owner = userRepository.save(owner);

        validJwtToken = tokenService.generateToken(owner);

        Book book = new Book();
        book.setIsbn("999888777");
        book.setTitle("The Great Feed");
        book.setAuthor("Data Author");
        book = bookRepository.save(book);

        UserBookStatus status = new UserBookStatus();
        status.setId(new UserBookStatusId(owner.getId(), book.getId()));
        status.setUser(owner);
        status.setBook(book);
        status.setLiked(true);
        status.setStatus(ReadingStatus.READING);
        status.setSentiment(BookSentiment.INSPIRING);
        userBookStatusRepository.save(status);

        Comment comment = new Comment("Amazing read!", owner, book);
        comment = commentRepository.save(comment);

        CommentLike like = new CommentLike(owner, comment);
        commentLikeRepository.save(like);
    }

    /**
     * Tests the retrieval of the paginated feed for an authenticated user.
     * Expects a 200 OK status and a fully populated JSON structure containing
     * book details, user specific interactions, and aggregated sentiment counts.
     *
     * @throws Exception if the mock MVC request fails.
     */
    @Test
    @DisplayName("GET /feed: Should return paginated feed with full user interactions")
    void shouldReturnFeedWithInteractions() throws Exception {
        mockMvc.perform(get("/vibebooks/api/feed")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("The Great Feed"))
                .andExpect(jsonPath("$.content[0].likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.content[0].status").value(ReadingStatus.READING.name()))
                .andExpect(jsonPath("$.content[0].sentiment").value(BookSentiment.INSPIRING.name()))
                .andExpect(jsonPath("$.content[0].sentimentCounts.INSPIRING").value(1))
                .andExpect(jsonPath("$.content[0].comments[0].text").value("Amazing read!"))
                .andExpect(jsonPath("$.content[0].comments[0].likesCount").value(1));
    }
}