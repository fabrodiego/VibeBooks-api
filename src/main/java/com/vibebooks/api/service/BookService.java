package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.ReadingStatus;
import com.vibebooks.api.model.User;
import com.vibebooks.api.model.UserBookStatus;
import com.vibebooks.api.model.UserBookStatusId;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserBookStatusRepository userBookStatusRepository;

    private static final String BOOK_NOT_FOUND = "Book not found";


    @Transactional(readOnly = true)
    public List<BookDetailsDTO> listAllBooks() {
        return bookRepository.findAll().stream().map(BookDetailsDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public BookDetailsDTO findBookById(UUID id) {
        return bookRepository.findById(id)
                .map(BookDetailsDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));
    }


    @Transactional
    public Book createBook(BookCreationDTO dto) {
        if (bookRepository.existsBookByIsbn(dto.isbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book with this ISBN already exists.");
        }
        var book = new Book(dto);
        return bookRepository.save(book);
    }

    @Transactional
    public BookDetailsDTO updateBook(UUID id, BookCreationDTO dto) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));
        book.updateInformation(dto);
        return new BookDetailsDTO(book);
    }

    @Transactional
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public UserBookStatus updateBookStatus(UUID bookId, User loggedInUser, BookStatusUpdateDTO dto) {
        var bookStatus = findOrCreateUserBookStatus(bookId, loggedInUser);

        if (dto.status() != null) {
            bookStatus.setStatus(dto.status());
        }

        if (dto.sentiment() != null) {
            if (bookStatus.getStatus() == ReadingStatus.WANT_TO_READ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only set a sentiment for books you are reading or have already read.");
            }
            bookStatus.setSentiment(dto.sentiment());
        }
        return userBookStatusRepository.saveAndFlush(bookStatus);
    }

    @Transactional
    public UserBookStatus likeOrUnlikeBook(UUID bookId, User loggedInUser) {
        var bookStatus = findOrCreateUserBookStatus(bookId, loggedInUser);

        bookStatus.setLiked(!bookStatus.isLiked());
        return userBookStatusRepository.saveAndFlush(bookStatus);
    }

    @Transactional(readOnly = true)
    public long countLikesForBook(UUID bookId) {
        return userBookStatusRepository.countByBookIdAndLikedIsTrue(bookId);
    }

    private Book getReferenceById(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        return bookRepository.getReferenceById(id);
    }

    private UserBookStatus findOrCreateUserBookStatus(UUID bookId, User user) {
        var book = getReferenceById(bookId);
        var statusId = new UserBookStatusId(user, book);

        return userBookStatusRepository.findById(statusId)
                .orElseGet(() -> {
                    var newStatus = new UserBookStatus();
                    newStatus.setId(statusId);
                    newStatus.setUser(user);
                    newStatus.setBook(book);
                    return newStatus;
                });
    }
}
