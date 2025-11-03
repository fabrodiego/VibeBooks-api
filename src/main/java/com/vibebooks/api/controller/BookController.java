package com.vibebooks.api.controller;

import com.vibebooks.api.dto.*;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

/**
 * REST controller for managing books and user interactions.
 * Provides endpoints for book CRUD operations, likes, statuses, and search.
 */
@RestController
@RequestMapping(API_PREFIX + "/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Returns a paginated list of books for the feed or library.
     *
     * @param pageable Pagination settings
     * @param loggedInUser Authenticated user
     * @return Page of books with user-specific data
     */
    @GetMapping
    public ResponseEntity<Page<BookDetailsDTO>> listBooks(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User loggedInUser
    ) {
        return ResponseEntity.ok(bookService.listAllBooks(pageable, loggedInUser));
    }

    /**
     * Retrieves detailed information about a specific book.
     *
     * @param id Book identifier
     * @param loggedInUser Authenticated user
     * @return Book details including user interactions
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> getBookById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedInUser
    ) {
        return ResponseEntity.ok(bookService.findBookById(id, loggedInUser));
    }

    /**
     * Creates a new book using Google Books API data.
     *
     * @param dto Request body with the book ISBN
     * @param uriBuilder Used to build the resource URI
     * @return Created book details
     */
    @PostMapping
    public ResponseEntity<BookDetailsDTO> createBook(
            @RequestBody @Valid BookIsbnDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        var newBook = bookService.createBook(dto);
        var uri = uriBuilder.path(API_PREFIX + "/books/{id}").buildAndExpand(newBook.getId()).toUri();
        long initialLikes = 0;
        boolean likedByUser = false;
        return ResponseEntity.created(uri)
                .body(new BookDetailsDTO(newBook, initialLikes, likedByUser, null, null));
    }

    /**
     * Updates book information such as title, author, or cover.
     *
     * @param id Book identifier
     * @param dto Request body with updated data
     * @param loggedInUser Authenticated user
     * @return Updated book details
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> updateBook(
            @PathVariable UUID id,
            @RequestBody @Valid BookCreationDTO dto,
            @AuthenticationPrincipal User loggedInUser
    ) {
        var updatedBook = bookService.updateBook(id, dto, loggedInUser);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Deletes a book and its user associations.
     *
     * @param id Book identifier
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Likes or unlikes a book for the authenticated user.
     *
     * @param id Book identifier
     * @param loggedInUser Authenticated user
     * @return Updated like state and total like count
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<BookLikeResponseDTO> likeOrUnlikeBook(
            @PathVariable UUID id,
            @AuthenticationPrincipal User loggedInUser
    ) {
        var bookStatus = bookService.likeOrUnlikeBook(id, loggedInUser);
        long totalLikes = bookService.countLikesForBook(id);
        return ResponseEntity.ok(new BookLikeResponseDTO(bookStatus.isLiked(), totalLikes));
    }

    /**
     * Updates the user's reading status or sentiment for a book.
     *
     * @param id Book identifier
     * @param dto Request body with status and/or sentiment
     * @param loggedInUser Authenticated user
     * @return Updated book status and sentiment
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<BookStatusResponseDTO> updateBookStatus(
            @PathVariable UUID id,
            @RequestBody @Valid BookStatusUpdateDTO dto,
            @AuthenticationPrincipal User loggedInUser
    ) {
        var savedStatus = bookService.updateBookStatus(id, loggedInUser, dto);
        var response = new BookStatusResponseDTO(
                savedStatus.getBook().getId(),
                savedStatus.getStatus(),
                savedStatus.getSentiment()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for books by title.
     *
     * @param query Search term
     * @param pageable Pagination settings
     * @param loggedInUser Authenticated user
     * @return Page of books matching the query
     */
    @GetMapping("/search")
    public ResponseEntity<Page<BookDetailsDTO>> searchBooks(
            @RequestParam("query") String query,
            @PageableDefault() Pageable pageable,
            @AuthenticationPrincipal User loggedInUser
    ) {
        return ResponseEntity.ok(bookService.searchBooks(query, pageable, loggedInUser));
    }
}
