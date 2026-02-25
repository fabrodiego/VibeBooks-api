package com.vibebooks.api.service;

import com.vibebooks.api.dto.CommentCreationDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.dto.PageResponseDTO;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.model.CommentLike;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentLikeRepository;
import com.vibebooks.api.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CommentService}.
 * Verifies business logic, authorization rules, and interactions with repositories.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @InjectMocks
    private CommentService commentService;

    private User loggedInUser;
    private Book validBook;
    private Comment validComment;
    private UUID bookId;
    private UUID commentId;

    @BeforeEach
    void setup() {
        bookId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        loggedInUser = new User();
        loggedInUser.setId(UUID.randomUUID());
        loggedInUser.setUsername("test_user");

        validBook = new Book();
        validBook.setId(bookId);
        validBook.setTitle("The Hobbit");

        validComment = new Comment("Great book!", loggedInUser, validBook);
        ReflectionTestUtils.setField(validComment, "id", commentId);
        ReflectionTestUtils.setField(validComment, "createdAt", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Add: Should add comment to book successfully")
    void shouldAddCommentSuccessfully() {
        CommentCreationDTO dto = new CommentCreationDTO("Great book!", bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(validBook));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        Comment result = commentService.addCommentToBook(dto, loggedInUser);

        assertNotNull(result);
        assertEquals("Great book!", result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Add: Should fail when book is not found")
    void shouldFailToAddCommentWhenBookNotFound() {
        CommentCreationDTO dto = new CommentCreationDTO("Great book!", bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.addCommentToBook(dto, loggedInUser));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Find: Should return paginated comments")
    void shouldFindCommentsByBookId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> commentPage = new PageImpl<>(List.of(validComment));

        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(commentRepository.findAllByBookId(bookId, pageable)).thenReturn(commentPage);
        when(commentLikeRepository.countByCommentId(commentId)).thenReturn(5L);
        when(commentLikeRepository.findByUserIdAndCommentId(loggedInUser.getId(), commentId))
                .thenReturn(Optional.of(new CommentLike()));

        PageResponseDTO<CommentDetailsDTO> result = commentService.findCommentsByBookId(bookId, loggedInUser, pageable);

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(5L, result.content().getFirst().likesCount());
        assertTrue(result.content().getFirst().likedByCurrentUser());
    }

    @Test
    @DisplayName("Find: Should fail when book does not exist")
    void shouldFailToFindCommentsWhenBookNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.existsById(bookId)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.findCommentsByBookId(bookId, loggedInUser, pageable));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Delete: Should delete comment successfully")
    void shouldDeleteCommentSuccessfully() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(validComment));

        assertDoesNotThrow(() -> commentService.deleteComment(commentId, loggedInUser));

        verify(commentLikeRepository, times(1)).deleteAll(any());
        verify(commentRepository, times(1)).delete(validComment);
    }

    @Test
    @DisplayName("Delete: Should fail when user does not own comment")
    void shouldFailToDeleteCommentWhenNotAuthorized() {
        User hackerUser = new User();
        hackerUser.setId(UUID.randomUUID());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(validComment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.deleteComment(commentId, hackerUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Like: Should add like to comment")
    void shouldLikeCommentSuccessfully() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(validComment));
        when(commentLikeRepository.findByUserIdAndCommentId(loggedInUser.getId(), commentId)).thenReturn(Optional.empty());
        when(commentLikeRepository.countByCommentId(commentId)).thenReturn(1L);

        CommentDetailsDTO result = commentService.likeOrUnlikeComment(commentId, loggedInUser);

        assertTrue(result.likedByCurrentUser());
        assertEquals(1L, result.likesCount());
        verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
    }

    @Test
    @DisplayName("Unlike: Should remove like from comment")
    void shouldUnlikeCommentSuccessfully() {
        CommentLike existingLike = new CommentLike(loggedInUser, validComment);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(validComment));
        when(commentLikeRepository.findByUserIdAndCommentId(loggedInUser.getId(), commentId)).thenReturn(Optional.of(existingLike));
        when(commentLikeRepository.countByCommentId(commentId)).thenReturn(0L);

        CommentDetailsDTO result = commentService.likeOrUnlikeComment(commentId, loggedInUser);

        assertFalse(result.likedByCurrentUser());
        assertEquals(0L, result.likesCount());
        verify(commentLikeRepository, times(1)).delete(existingLike);
    }
}