package com.vibebooks.api.controller;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.dto.BookLikeResponseDTO;
import com.vibebooks.api.dto.BookIsbnDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.model.UserBookStatus;
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

@RestController
@RequestMapping(API_PREFIX + "/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Page<BookDetailsDTO>> listBooks(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookService.listAllBooks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.findBookById(id));
    }

    @PostMapping
    public ResponseEntity<BookDetailsDTO> createBook(@RequestBody @Valid BookIsbnDTO dto, UriComponentsBuilder uriBuilder) {
        var newBook = bookService.createBook(dto);
        var uri = uriBuilder.path(API_PREFIX + "/books/{id}").buildAndExpand(newBook.getId()).toUri();
        return ResponseEntity.created(uri).body(new BookDetailsDTO(newBook));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> updateBook(@PathVariable UUID id, @RequestBody @Valid BookCreationDTO dto) {
        var updatedBook = bookService.updateBook(id, dto);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints (Book Interactions) ---

    @PostMapping("/{id}/like")
    public ResponseEntity<BookLikeResponseDTO> likeOrUnlikeBook(@PathVariable UUID id, @AuthenticationPrincipal User loggedInUser) {
        var bookStatus = bookService.likeOrUnlikeBook(id, loggedInUser);
        long totalLikes = bookService.countLikesForBook(id);
        return ResponseEntity.ok(new BookLikeResponseDTO(bookStatus.isLiked(), totalLikes));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<UserBookStatus> updateBookStatus(
            @PathVariable UUID id,
            @RequestBody @Valid BookStatusUpdateDTO dto,
            @AuthenticationPrincipal User loggedInUser) {
        var savedStatus = bookService.updateBookStatus(id, loggedInUser, dto);
        return ResponseEntity.ok(savedStatus);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookDetailsDTO>> searchBooks(
            @RequestParam("query") String query,
            @PageableDefault() Pageable pageable) {

        return ResponseEntity.ok(bookService.searchBooks(query, pageable));
    }
}
