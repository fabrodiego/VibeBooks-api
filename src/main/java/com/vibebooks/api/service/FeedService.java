package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookFeedDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.dto.PageResponseDTO;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public PageResponseDTO<BookFeedDTO> getBookFeed(Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAll(pageable);

        List<UUID> bookIds = booksPage.getContent().stream()
                .map(Book::getId)
                .toList();

        List<Comment> comments = commentRepository.findAllByBookIdIn(bookIds);

        Map<UUID, List<CommentDetailsDTO>> commentsByBookId = comments.stream()
                .map(CommentDetailsDTO::new)
                .collect(Collectors.groupingBy(CommentDetailsDTO::bookId));

        Page<BookFeedDTO> feedDtoPage = booksPage.map(book -> {
            List<CommentDetailsDTO> bookComments = commentsByBookId.getOrDefault(book.getId(), List.of());
            return new BookFeedDTO(book, bookComments);
        });

        return new PageResponseDTO<>(feedDtoPage);
    }
}