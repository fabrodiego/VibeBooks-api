package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.dto.BookIsbnDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.dto.google.GoogleBooksResponse;
import com.vibebooks.api.dto.google.VolumeInfo;
import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.ReadingStatus;
import com.vibebooks.api.model.User;
import com.vibebooks.api.model.UserBookStatus;
import com.vibebooks.api.model.UserBookStatusId;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserBookStatusRepository userBookStatusRepository;
    private final RestTemplate restTemplate;

    @Value("${google.books.api.key}")
    private String apiKey;

    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s&key=%s";

    private static final String BOOK_NOT_FOUND = "Book not found";

    @Transactional(readOnly = true)
    public Page<BookDetailsDTO> listAllBooks(Pageable pageable, User loggedInUser) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.map(book -> {
            long totalLikes = countLikesForBook(book.getId());
            boolean likedByUser = isBookLikedByUser(book.getId(), loggedInUser);
            return new BookDetailsDTO(book, totalLikes, likedByUser);
        });
    }

    @Transactional(readOnly = true)
    public BookDetailsDTO findBookById(UUID id, User loggedInUser) {
        return bookRepository.findById(id)
                .map(book -> {
                    long totalLikes = countLikesForBook(book.getId());
                    boolean likedByUser = isBookLikedByUser(id, loggedInUser);
                    return new BookDetailsDTO(book, totalLikes, likedByUser);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));
    }


    @Transactional
    public Book createBook(BookIsbnDTO dto) {
        if (bookRepository.existsByIsbn(dto.isbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book with this ISBN already exists...");
        }
        var volumeInfo = fetchVolumeInfoFromGoogle(dto.isbn());

        var book = Book.fromGoogleVolumeInfo(volumeInfo, dto.isbn());

        return bookRepository.save(book);
    }

    @Transactional
    public BookDetailsDTO updateBook(UUID id, BookCreationDTO dto, User loggedInUser) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));
        book.updateInformation(dto);
        long totalLikes = countLikesForBook(id);
        boolean likedByUser = isBookLikedByUser(id, loggedInUser);
        return new BookDetailsDTO(book, totalLikes, likedByUser);
    }

    @Transactional
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        userBookStatusRepository.deleteByBookId(id);
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

    private boolean isBookLikedByUser(UUID bookId, User loggedInUser) {
        if (loggedInUser == null) {
            return false;
        }
        var statusId = new UserBookStatusId(loggedInUser.getId(), bookId);
        return userBookStatusRepository.findById(statusId)
                .map(UserBookStatus::isLiked)
                .orElse(false);
    }

    private Book getReferenceById(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        return bookRepository.getReferenceById(id);
    }

    private UserBookStatus findOrCreateUserBookStatus(UUID bookId, User user) {
        var book = getReferenceById(bookId);
        var statusId = UserBookStatusId.of(user, book);
        return userBookStatusRepository.findById(statusId)
                .orElseGet(() -> {
                    var newStatus = new UserBookStatus();
                    newStatus.setId(statusId);
                    newStatus.setUser(user);
                    newStatus.setBook(book);
                    return userBookStatusRepository.save(newStatus);
                });
    }

    @Transactional(readOnly = true)
    public Page<BookDetailsDTO> searchBooks(String query, Pageable pageable, User loggedInUser) {
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCase(query, pageable);
        return bookPage.map(book -> {
            long totalLikes = countLikesForBook(book.getId());
            boolean likedByUser = isBookLikedByUser(book.getId(), loggedInUser);
            return new BookDetailsDTO(book, totalLikes, likedByUser);
        });
    }

    private VolumeInfo fetchVolumeInfoFromGoogle(String isbn) {
        String url = String.format(GOOGLE_BOOKS_API_URL, isbn, apiKey);
        GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found on Google Books for the provided ISBN.");
        }
        return response.items().getFirst().volumeInfo();
    }
}
