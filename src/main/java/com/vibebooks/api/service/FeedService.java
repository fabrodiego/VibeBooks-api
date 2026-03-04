package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookFeedDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.dto.PageResponseDTO;
import com.vibebooks.api.model.*;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentLikeRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for generating the user feed.
 * * Business Rule: Aggregates books, user interactions (likes, reading status, sentiments),
 * and comments in a single paginated response. To prevent N+1 database performance issues,
 * it strictly uses batch fetching (SQL IN clauses) and groups the data in memory.
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserBookStatusRepository userBookStatusRepository;

    /**
     * Retrieves a paginated feed of books tailored to the current user.
     *
     * @param pageable Pagination configuration.
     * @param loggedInUser The currently authenticated user, or null if anonymous.
     * @return A paginated wrapper containing the fully aggregated book feed.
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<BookFeedDTO> getBookFeed(Pageable pageable, User loggedInUser) {
        Page<Book> booksPage = bookRepository.findAll(pageable);
        List<UUID> bookIds = booksPage.getContent().stream().map(Book::getId).toList();

        if (bookIds.isEmpty()) {
            return new PageResponseDTO<>(booksPage.map(b -> null));
        }

        Map<UUID, Long> bookLikesCount = userBookStatusRepository.countLikesByBookIdIn(bookIds).stream()
                .collect(Collectors.toMap(UserBookStatusRepository.BookLikeCount::getBookId, UserBookStatusRepository.BookLikeCount::getCount));

        Map<UUID, Map<BookSentiment, Long>> bookSentiments = new HashMap<>();
        for (UUID id : bookIds) {
            Map<BookSentiment, Long> initial = new EnumMap<>(BookSentiment.class);
            for (BookSentiment s : BookSentiment.values()) initial.put(s, 0L);
            bookSentiments.put(id, initial);
        }
        userBookStatusRepository.countSentimentsByBookIdIn(bookIds).forEach(agg ->
                bookSentiments.get(agg.getBookId()).put(agg.getSentiment(), agg.getCount())
        );

        Map<UUID, UserBookStatus> userStatuses = new HashMap<>();
        if (loggedInUser != null) {
            userBookStatusRepository.findAllByUserIdAndBookIdIn(loggedInUser.getId(), bookIds)
                    .forEach(status -> userStatuses.put(status.getBook().getId(), status));
        }

        List<Comment> comments = commentRepository.findAllByBookIdIn(bookIds);
        List<UUID> commentIds = comments.stream().map(Comment::getId).toList();

        Map<UUID, Long> commentLikesCount = new HashMap<>();
        Set<UUID> commentsLikedByUser = new HashSet<>();

        if (!commentIds.isEmpty()) {
            commentLikeRepository.countLikesByCommentIdIn(commentIds).forEach(agg ->
                    commentLikesCount.put(agg.getCommentId(), agg.getCount())
            );

            if (loggedInUser != null) {
                commentLikeRepository.findAllByUserIdAndCommentIdIn(loggedInUser.getId(), commentIds)
                        .forEach(cl -> commentsLikedByUser.add(cl.getComment().getId()));
            }
        }

        Map<UUID, List<CommentDetailsDTO>> commentsByBook = comments.stream()
                .map(c -> new CommentDetailsDTO(
                        c,
                        commentLikesCount.getOrDefault(c.getId(), 0L),
                        commentsLikedByUser.contains(c.getId())
                ))
                .collect(Collectors.groupingBy(CommentDetailsDTO::bookId));

        Page<BookFeedDTO> feedDtoPage = booksPage.map(book -> {
            UUID bId = book.getId();
            UserBookStatus uStatus = userStatuses.get(bId);

            return new BookFeedDTO(
                    book,
                    commentsByBook.getOrDefault(bId, List.of()),
                    bookLikesCount.getOrDefault(bId, 0L),
                    uStatus != null && uStatus.isLiked(),
                    uStatus != null ? uStatus.getStatus() : null,
                    uStatus != null ? uStatus.getSentiment() : null,
                    bookSentiments.get(bId)
            );
        });

        return new PageResponseDTO<>(feedDtoPage);
    }
}