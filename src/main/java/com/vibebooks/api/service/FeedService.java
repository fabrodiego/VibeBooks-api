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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that provides the main user feed with books, comments, and interactions.
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserBookStatusRepository userBookStatusRepository;

    /**
     * Returns a paginated list of books for the user feed,
     * including likes, comments, reading status, and sentiment.
     *
     * @param pageable Pagination config
     * @param loggedInUser Current authenticated user
     * @return Paginated book feed with personalized data
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<BookFeedDTO> getBookFeed(Pageable pageable, User loggedInUser) {
        Page<Book> booksPage = bookRepository.findAll(pageable);

        List<UUID> bookIds = booksPage.getContent().stream()
                .map(Book::getId)
                .toList();

        List<Comment> comments = commentRepository.findAllByBookIdIn(bookIds);

        Map<UUID, List<CommentDetailsDTO>> commentsByBookId = comments.stream()
                .map(comment -> {
                    long likesCount = commentLikeRepository.countByCommentId(comment.getId());
                    boolean likedByCurrentUser = (loggedInUser != null) &&
                            commentLikeRepository.findByUserIdAndCommentId(loggedInUser.getId(), comment.getId()).isPresent();
                    return new CommentDetailsDTO(comment, likesCount, likedByCurrentUser);
                })
                .collect(Collectors.groupingBy(CommentDetailsDTO::bookId));

        Page<BookFeedDTO> feedDtoPage = booksPage.map(book -> {
            List<CommentDetailsDTO> bookComments = commentsByBookId.getOrDefault(book.getId(), List.of());

            long bookLikesCount = userBookStatusRepository.countByBookIdAndLikedIsTrue(book.getId());
            boolean bookLikedByUser = (loggedInUser != null) &&
                    userBookStatusRepository.findById(new UserBookStatusId(loggedInUser.getId(), book.getId()))
                            .map(UserBookStatus::isLiked)
                            .orElse(false);

            ReadingStatus userStatus = (loggedInUser != null)
                    ? userBookStatusRepository.findById(new UserBookStatusId(loggedInUser.getId(), book.getId()))
                    .map(UserBookStatus::getStatus)
                    .orElse(null)
                    : null;

            BookSentiment userSentiment = (loggedInUser != null)
                    ? userBookStatusRepository.findById(new UserBookStatusId(loggedInUser.getId(), book.getId()))
                    .map(UserBookStatus::getSentiment)
                    .orElse(null)
                    : null;

            return new BookFeedDTO(book, bookComments, bookLikesCount, bookLikedByUser, userStatus, userSentiment);
        });

        return new PageResponseDTO<>(feedDtoPage);
    }
}