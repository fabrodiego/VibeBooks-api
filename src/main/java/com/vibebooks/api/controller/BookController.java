package com.vibebooks.api.controller;

import com.vibebooks.api.dto.CommentCreationDTO;
import com.vibebooks.api.dto.BookLikeResponseDTO;
import com.vibebooks.api.dto.CommentDetailsDTO;
import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.model.Comment;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.User;
import com.vibebooks.api.model.UserBookStatus;
import com.vibebooks.api.model.UserBookStatusId;
import com.vibebooks.api.repository.UserBookStatusRepository;
import com.vibebooks.api.repository.CommentRepository;
import com.vibebooks.api.repository.BookRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final UserBookStatusRepository userBookStatusRepository;

    public BookController(BookRepository bookRepository, CommentRepository commentRepository, UserBookStatusRepository userBookStatusRepository) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.userBookStatusRepository = userBookStatusRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BookDetailsDTO> registerBook(@RequestBody @Valid BookCreationDTO data, UriComponentsBuilder uriBuilder) {
        // 1. Converts the received DTO to the Book entity
        var book = new Book(data);

        // 2. Saves the new entity in the db
        bookRepository.save(book);

        // 3. Creates the return URI
        var uri = uriBuilder.path("/api/books/{id}").buildAndExpand(book.getId()).toUri();

        // 4. Returns status 201 Created with the URI and the recently registered book's data
        return ResponseEntity.created(uri).body(new BookDetailsDTO(book));
    }

    @GetMapping
    public ResponseEntity<List<BookDetailsDTO>> listBooks() {
        // 1. Fetches all books from the repository
        var books = bookRepository.findAll();

        // 2. Converts the list of Book entities to a list of detail DTOs
        var dtos = books.stream().map(BookDetailsDTO::new).toList();

        // 3. Returns the list with status 200 OK
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailsDTO> getBookDetails(@PathVariable UUID id) {
        // 1. Fetches the book from the repository by "ID"
        var optionalBook = bookRepository.findById(id);

        // 2. If the book is not found, returns status 404 Not Found
        if (optionalBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. If found, converts to the detail DTO and returns with status 200 OK
        var book = optionalBook.get();
        return ResponseEntity.ok(new BookDetailsDTO(book));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BookDetailsDTO> updateBook(@PathVariable UUID id, @RequestBody @Valid BookCreationDTO data){
        // 1. Fetches the book we want to update from the db
        var optionalBook = bookRepository.findById(id);

        // 2. If not found, returns 404 Not Found
        if (optionalBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 3. If found, updates the book's data with the information received in the DTO
        var book = optionalBook.get();
        book.updateInformation(data);

        // 4. JPA already understands that the object was modified within the transaction
        // and will automatically save the changes to the db when the method flow finishes.

        // 5. Returns 200 OK with the updated book data
        return ResponseEntity.ok(new BookDetailsDTO(book));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        // 1. Before deleting, we check if the book really exists
        if (!bookRepository.existsById(id)) {
            // 2. If it doesn't exist, we return 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // 3. If the book exists, we tell the repository to delete it
        bookRepository.deleteById(id);

        // 4. We return status 204 No Content
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @Transactional
    public ResponseEntity<CommentDetailsDTO> addComment(
            @PathVariable UUID id,
            @RequestBody @Valid CommentCreationDTO data,
            Authentication authentication,
            UriComponentsBuilder uriBuilder
    ) {
        var book = bookRepository.getReferenceById(id);

        User loggedInUser = (User)  authentication.getPrincipal();

        var comment = new Comment(data.text(), loggedInUser, book);

        var savedComment = commentRepository.saveAndFlush(comment);

        var uri = uriBuilder.path("/api/comments/{id}").buildAndExpand(savedComment.getId()).toUri();

        return ResponseEntity.created(uri).body(new CommentDetailsDTO(savedComment));
    }

    /**
     * Endpoint to list all comments for a specific book.
     *
     * @param id The book's "ID".
     * @return A list of comment detail DTOs, or 404 Not Found if the book doesn't exist.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDetailsDTO>> listCommentsByBook(@PathVariable UUID id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        var comments = commentRepository.findAllByBookId(id);

        var dtos = comments.stream().map(CommentDetailsDTO::new).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<BookLikeResponseDTO> likeOrUnlikeBook(@PathVariable UUID id, Authentication authentication) {
        var loggedInUser = (User) authentication.getPrincipal();

        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        var statusId = new UserBookStatusId(loggedInUser.getId(), book.getId());

        var status = userBookStatusRepository.findById(statusId).orElseGet(() -> {
            var newStatus = new UserBookStatus();
            newStatus.setId(statusId);
            newStatus.setUser(loggedInUser);
            newStatus.setBook(book);
            return newStatus;
        });

        status.setLiked(!status.isLiked());
        userBookStatusRepository.saveAndFlush(status);

        long totalLikes = userBookStatusRepository.countByBookIdAndLikedIsTrue(book.getId());

        return ResponseEntity.ok(new BookLikeResponseDTO(status.isLiked(), totalLikes));
    }
}
