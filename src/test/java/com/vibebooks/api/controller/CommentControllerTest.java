package com.vibebooks.api.controller;

import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.UserRepository;
import com.vibebooks.api.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CommentRepository commentRepository;

    /**
     * Test case to verify that the owner of a comment can successfully delete their comment.
     * It expects a 204 No Content status and asserts that the comment no longer exists in the repository.
     *
     * @throws Exception if an error occurs during the mock MVC request.
     */
    @Test
    @DisplayName("Should allow owner to delete their comment and return 204 No Content")
    void deleteComment_AsOwner_ShouldSucceed() throws Exception {
        User owner = userRepository.save(new User("owner", "owner@email.com", "hashed_password"));
        Book book = bookRepository.save(new Book("Test Book", "Test Author"));
        Comment comment = commentRepository.save(new Comment("My own comment", owner, book));
        // Ensure the comment is saved before proceeding
        commentRepository.flush();

        String ownerToken = tokenService.generateToken(owner);

        var response = mockMvc.perform(
                delete("/vibebooks/api/comments/{id}", comment.getId())
                        .header("Authorization", "Bearer " + ownerToken)
        );

        response.andExpect(status().isNoContent());

        boolean commentExists = commentRepository.existsById(comment.getId());
        assertThat(commentExists).isFalse();
    }

    /**
     * Test case to verify that a user attempting to delete another user's comment
     * receives a 403 Forbidden status. It also asserts that the comment remains in the repository.
     *
     * @throws Exception if an error occurs during the mock MVC request.
     */
    @Test
    @DisplayName("Should return 403 Forbidden when user tries to delete another user's comment")
    void deleteComment_AsAnotherUser_ShouldReturn403() throws Exception {
        User owner = userRepository.save(new User("owner", "owner@email.com", "hashed_password"));
        User attacker = userRepository.save(new User("attacker", "attacker@email.com", "hashed_password"));
        Book book = bookRepository.save(new Book("Test Book", "Test Author"));
        Comment ownersComment = commentRepository.save(new Comment("A comment to protect", owner, book));
        commentRepository.flush();

        String attackerToken = tokenService.generateToken(attacker);

        var response = mockMvc.perform(
                delete("/vibebooks/api/comments/{id}", ownersComment.getId())
                        .header("Authorization", "Bearer " + attackerToken)
        );

        response.andExpect(status().isForbidden());

        boolean commentExists = commentRepository.existsById(ownersComment.getId());
        assertThat(commentExists).isTrue();
    }

}