package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookFeedDTO;
import com.vibebooks.api.dto.PageResponseDTO;
import com.vibebooks.api.dto.SentimentCountDTO;
import com.vibebooks.api.model.*;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentLikeRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private UserBookStatusRepository userBookStatusRepository;

    @InjectMocks
    private FeedService feedService;

    private User loggedInUser;
    private Book validBook;
    private Comment validComment;

    @BeforeEach
    void setup() {
        loggedInUser = new User();
        loggedInUser.setId(UUID.randomUUID());

        validBook = new Book();
        validBook.setId(UUID.randomUUID());
        validBook.setTitle("Feed Book");

        validComment = new Comment("Great!", loggedInUser, validBook);
        validComment.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("GetFeed: Should return feed with user interactions")
    void shouldReturnFeedForAuthenticatedUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(validBook));

        UserBookStatus status = new UserBookStatus();
        status.setLiked(true);
        status.setStatus(ReadingStatus.READING);
        status.setSentiment(BookSentiment.INSPIRING);

        // Mocks
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(commentRepository.findAllByBookIdIn(List.of(validBook.getId()))).thenReturn(List.of(validComment));
        when(commentLikeRepository.countByCommentId(validComment.getId())).thenReturn(10L);
        when(commentLikeRepository.findByUserIdAndCommentId(loggedInUser.getId(), validComment.getId()))
                .thenReturn(Optional.of(new CommentLike()));
        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBook.getId())).thenReturn(5L);
        when(userBookStatusRepository.findById(any(UserBookStatusId.class))).thenReturn(Optional.of(status));
        when(userBookStatusRepository.countSentimentsByBookId(validBook.getId()))
                .thenReturn(List.of(new SentimentCountDTO(BookSentiment.INSPIRING, 2L)));

        PageResponseDTO<BookFeedDTO> result = feedService.getBookFeed(pageable, loggedInUser);

        assertNotNull(result);
        assertEquals(1, result.content().size());

        BookFeedDTO feedDTO = result.content().getFirst();
        assertEquals("Feed Book", feedDTO.title());
        assertTrue(feedDTO.likedByCurrentUser());
        assertEquals(5L, feedDTO.likesCount());
        assertEquals(ReadingStatus.READING, feedDTO.status());
        assertEquals(BookSentiment.INSPIRING, feedDTO.sentiment());

        assertEquals(1, feedDTO.comments().size());
        assertEquals(10L, feedDTO.comments().getFirst().likesCount());
        assertTrue(feedDTO.comments().getFirst().likedByCurrentUser());

        assertEquals(2L, feedDTO.sentimentCounts().get(BookSentiment.INSPIRING));
    }

    @Test
    @DisplayName("GetFeed: Should return feed without user interactions when not logged in")
    void shouldReturnFeedForAnonymousUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(validBook));

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(commentRepository.findAllByBookIdIn(anyList())).thenReturn(List.of());
        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBook.getId())).thenReturn(3L);
        when(userBookStatusRepository.countSentimentsByBookId(validBook.getId())).thenReturn(List.of());

        // Execute with NULL user
        PageResponseDTO<BookFeedDTO> result = feedService.getBookFeed(pageable, null);

        assertNotNull(result);
        BookFeedDTO feedDTO = result.content().getFirst();
        assertEquals("Feed Book", feedDTO.title());
        assertFalse(feedDTO.likedByCurrentUser());
        assertEquals(3L, feedDTO.likesCount());
        assertNull(feedDTO.status());
        assertNull(feedDTO.sentiment());
        assertTrue(feedDTO.comments().isEmpty());
    }
}