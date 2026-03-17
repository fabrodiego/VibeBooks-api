package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookFeedDTO;
import com.vibebooks.api.dto.PageResponseDTO;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FeedService}.
 * * Business Rule Verification: Ensures that batch data aggregation (mocking projection interfaces)
 * correctly maps to the final BookFeedDTO for both authenticated and anonymous users.
 */
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

    /**
     * Tests the feed generation for an authenticated user.
     * Expects all personalized interactions (likes, status, sentiments) to be correctly mapped.
     */
    @Test
    @DisplayName("GetFeed: Should return feed with user interactions")
    void shouldReturnFeedForAuthenticatedUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(validBook));
        List<UUID> bookIds = List.of(validBook.getId());
        List<UUID> commentIds = List.of(validComment.getId());

        UserBookStatus status = new UserBookStatus();
        status.setBook(validBook);
        status.setUser(loggedInUser);
        status.setLiked(true);
        status.setStatus(ReadingStatus.READING);
        status.setSentiment(BookSentiment.INSPIRING);

        // Mocks for Book
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        UserBookStatusRepository.BookLikeCount bookLikeMock = mock(UserBookStatusRepository.BookLikeCount.class);
        when(bookLikeMock.getBookId()).thenReturn(validBook.getId());
        when(bookLikeMock.getCount()).thenReturn(5L);
        when(userBookStatusRepository.countLikesByBookIdIn(bookIds)).thenReturn(List.of(bookLikeMock));

        UserBookStatusRepository.BookSentimentAggregation sentimentMock = mock(UserBookStatusRepository.BookSentimentAggregation.class);
        when(sentimentMock.getBookId()).thenReturn(validBook.getId());
        when(sentimentMock.getSentiment()).thenReturn(BookSentiment.INSPIRING);
        when(sentimentMock.getCount()).thenReturn(2L);
        when(userBookStatusRepository.countSentimentsByBookIdIn(bookIds)).thenReturn(List.of(sentimentMock));

        when(userBookStatusRepository.findAllByUserIdAndBookIdIn(loggedInUser.getId(), bookIds)).thenReturn(List.of(status));

        // Mocks for Comment
        when(commentRepository.findAllByBookIdIn(bookIds)).thenReturn(List.of(validComment));

        CommentLikeRepository.CommentLikeCount commentLikeMock = mock(CommentLikeRepository.CommentLikeCount.class);
        when(commentLikeMock.getCommentId()).thenReturn(validComment.getId());
        when(commentLikeMock.getCount()).thenReturn(10L);
        when(commentLikeRepository.countLikesByCommentIdIn(commentIds)).thenReturn(List.of(commentLikeMock));

        CommentLike cl = new CommentLike(loggedInUser, validComment);
        when(commentLikeRepository.findAllByUserIdAndCommentIdIn(loggedInUser.getId(), commentIds)).thenReturn(List.of(cl));

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

    /**
     * Tests the feed generation for an anonymous user (not logged in).
     * Expects public data to be present, but user-specific interaction flags to be false or null.
     */
    @Test
    @DisplayName("GetFeed: Should return feed without user interactions when not logged in")
    void shouldReturnFeedForAnonymousUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(List.of(validBook));
        List<UUID> bookIds = List.of(validBook.getId());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        UserBookStatusRepository.BookLikeCount bookLikeMock = mock(UserBookStatusRepository.BookLikeCount.class);
        when(bookLikeMock.getBookId()).thenReturn(validBook.getId());
        when(bookLikeMock.getCount()).thenReturn(3L);
        when(userBookStatusRepository.countLikesByBookIdIn(bookIds)).thenReturn(List.of(bookLikeMock));

        when(userBookStatusRepository.countSentimentsByBookIdIn(bookIds)).thenReturn(List.of());
        when(commentRepository.findAllByBookIdIn(bookIds)).thenReturn(List.of());

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