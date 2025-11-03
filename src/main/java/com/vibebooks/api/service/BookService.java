package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.dto.BookIsbnDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.dto.google.GoogleBooksResponse;
import com.vibebooks.api.dto.google.VolumeInfo;
import com.vibebooks.api.model.*;
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

import java.util.Optional;
import java.util.UUID;


/**
 * Service class responsible for handling all business logic related to books,
 * including creation, retrieval, updating, deletion, likes, and user-specific status.
 *
 * <p>This class connects the controller layer with the persistence layer
 * and ensures that all operations follow the domain rules of the system.</p>
 */
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


    /**
     * Retrieves a paginated list of all books available in the system.
     * Each book is converted into a {@link BookDetailsDTO} enriched with
     * user-specific data (likes, reading status, and sentiment).
     *
     * @param pageable      Pagination configuration (page, size, sorting)
     * @param loggedInUser  The authenticated user
     * @return Page of BookDetailsDTO with personalized data for the user
     */
    @Transactional(readOnly = true)
    public Page<BookDetailsDTO> listAllBooks(Pageable pageable, User loggedInUser) {
        Page<Book> bookPage = bookRepository.findAll(pageable);
        return bookPage.map(book -> toBookDetailsDTO(book, loggedInUser));
    }

    /**
     * Finds a specific book by its unique identifier.
     * Includes user-specific information (likes, status, sentiment).
     *
     * @param id            UUID of the book
     * @param loggedInUser  The authenticated user
     * @return Complete BookDetailsDTO representation of the book
     */
    @Transactional(readOnly = true)
    public BookDetailsDTO findBookById(UUID id, User loggedInUser) {
        return bookRepository.findById(id)
                .map(book -> {
                    long totalLikes = countLikesForBook(book.getId());
                    boolean likedByUser = isBookLikedByUser(id, loggedInUser);
                    ReadingStatus userStatus = null;
                    BookSentiment userSentiment = null;

                    if (loggedInUser != null) {
                        var statusId = new UserBookStatusId(loggedInUser.getId(), id);
                        var userBookStatus = userBookStatusRepository.findById(statusId);
                        if (userBookStatus.isPresent()) {
                            userStatus = userBookStatus.get().getStatus();
                            userSentiment = userBookStatus.get().getSentiment();
                        }
                    }

                    return new BookDetailsDTO(book, totalLikes, likedByUser, userStatus, userSentiment);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));
    }

    /**
     * Creates a new book by fetching its details from the Google Books API.
     *
     * @param dto Object containing the ISBN used for retrieval
     * @return The persisted {@link Book} entity
     */
    @Transactional
    public Book createBook(BookIsbnDTO dto) {
        if (bookRepository.existsByIsbn(dto.isbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book with this ISBN already exists...");
        }

        var volumeInfo = fetchVolumeInfoFromGoogle(dto.isbn());
        var book = Book.fromGoogleVolumeInfo(volumeInfo, dto.isbn());

        return bookRepository.save(book);
    }

    /**
     * Updates an existing book’s information such as title, author, or cover image.
     * Returns the full updated {@link BookDetailsDTO}.
     *
     * @param id            The ID of the book to update
     * @param dto           DTO with new information
     * @param loggedInUser  Authenticated user
     * @return Updated BookDetailsDTO
     */
    @Transactional
    public BookDetailsDTO updateBook(UUID id, BookCreationDTO dto, User loggedInUser) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND));

        book.updateInformation(dto);

        long totalLikes = countLikesForBook(id);
        boolean likedByUser = isBookLikedByUser(id, loggedInUser);
        Optional<UserBookStatus> userBookStatus = getUserBookStatus(id, loggedInUser);
        ReadingStatus status = userBookStatus.map(UserBookStatus::getStatus).orElse(null);
        BookSentiment sentiment = userBookStatus.map(UserBookStatus::getSentiment).orElse(null);

        return new BookDetailsDTO(book, totalLikes, likedByUser, status, sentiment);
    }

    /**
     * Deletes a book from the database and removes all associated user statuses.
     *
     * @param id UUID of the book to be deleted
     */
    @Transactional
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        userBookStatusRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }

    /**
     * Updates the reading status or sentiment for a specific book related to the logged-in user.
     * Sentiment can only be added to books that are currently being read or have already been read.
     *
     * @param bookId        The book’s UUID
     * @param loggedInUser  The authenticated user
     * @param dto           DTO containing the new status and/or sentiment
     * @return Updated {@link UserBookStatus} entity
     */
    @Transactional
    public UserBookStatus updateBookStatus(UUID bookId, User loggedInUser, BookStatusUpdateDTO dto) {
        var bookStatus = getOrCreateUserBookStatus(bookId, loggedInUser);

        bookStatus.setStatus(dto.status());

        if (dto.sentiment() != null) {
            ReadingStatus currentStatus = bookStatus.getStatus();

            if (currentStatus == null || currentStatus == ReadingStatus.WANT_TO_READ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only set a sentiment for books you are reading or have already read.");
            }
            bookStatus.setSentiment(dto.sentiment());
        } else {
            bookStatus.setSentiment(null);
        }

        if (bookStatus.getStatus() == null || bookStatus.getStatus() == ReadingStatus.WANT_TO_READ) {
            bookStatus.setSentiment(null);
        }

        return userBookStatusRepository.saveAndFlush(bookStatus);
    }

    /**
     * Toggles the like status of a book for the logged-in user.
     * If liked, it becomes unliked, and vice versa.
     *
     * @param bookId        UUID of the book
     * @param loggedInUser  Authenticated user
     * @return Updated {@link UserBookStatus}
     */
    @Transactional
    public UserBookStatus likeOrUnlikeBook(UUID bookId, User loggedInUser) {
        var bookStatus = getOrCreateUserBookStatus(bookId, loggedInUser);
        bookStatus.setLiked(!bookStatus.isLiked());
        return userBookStatusRepository.saveAndFlush(bookStatus);
    }

    /**
     * Searches for books that contain the given query string in their title.
     * Returns results personalized with user-specific info.
     *
     * @param query         Search term for book titles
     * @param pageable      Pagination parameters
     * @param loggedInUser  Authenticated user
     * @return Page of {@link BookDetailsDTO} objects
     */
    @Transactional(readOnly = true)
    public Page<BookDetailsDTO> searchBooks(String query, Pageable pageable, User loggedInUser) {
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCase(query, pageable);
        return bookPage.map(book -> toBookDetailsDTO(book, loggedInUser));
    }

    /**
     * Counts how many likes a given book has received from all users.
     *
     * @param bookId UUID of the book
     * @return Total like count
     */
    public long countLikesForBook(UUID bookId) {
        return userBookStatusRepository.countByBookIdAndLikedIsTrue(bookId);
    }

    /**
     * Checks if the given user has liked the specified book.
     *
     * @param bookId        UUID of the book
     * @param loggedInUser  Authenticated user
     * @return true if liked, false otherwise
     */
    private boolean isBookLikedByUser(UUID bookId, User loggedInUser) {
        if (loggedInUser == null) {
            return false;
        }
        var statusId = new UserBookStatusId(loggedInUser.getId(), bookId);
        return userBookStatusRepository.findById(statusId)
                .map(UserBookStatus::isLiked)
                .orElse(false);
    }

    /**
     * Retrieves a lightweight reference of a book from the database.
     * Throws an exception if the book doesn’t exist.
     *
     * @param id UUID of the book
     * @return The {@link Book} entity reference
     */
    private Book getReferenceById(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BOOK_NOT_FOUND);
        }
        return bookRepository.getReferenceById(id);
    }

    /**
     * Retrieves the current {@link UserBookStatus} for a user and book.
     * If no record exists, a new one is created and persisted.
     *
     * @param bookId Book UUID
     * @param user   Authenticated user
     * @return Existing or newly created {@link UserBookStatus}
     */
    private UserBookStatus getOrCreateUserBookStatus(UUID bookId, User user) {
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

    /**
     * Retrieves the {@link UserBookStatus} for the user if it exists.
     *
     * @param bookId        Book UUID
     * @param loggedInUser  Authenticated user
     * @return Optional containing the status, or empty if not found
     */
    private Optional<UserBookStatus> getUserBookStatus(UUID bookId, User loggedInUser) {
        if (loggedInUser == null) return Optional.empty();
        return userBookStatusRepository.findById(new UserBookStatusId(loggedInUser.getId(), bookId));
    }

    /**
     * Builds a complete {@link BookDetailsDTO} for a given book and user.
     * This method centralizes the logic to avoid code duplication.
     *
     * @param book          The Book entity
     * @param loggedInUser  The authenticated user
     * @return Fully constructed BookDetailsDTO
     */
    private BookDetailsDTO toBookDetailsDTO(Book book, User loggedInUser) {
        long totalLikes = countLikesForBook(book.getId());
        boolean likedByUser = isBookLikedByUser(book.getId(), loggedInUser);

        Optional<UserBookStatus> userBookStatus = getUserBookStatus(book.getId(), loggedInUser);
        ReadingStatus status = userBookStatus.map(UserBookStatus::getStatus).orElse(null);
        BookSentiment sentiment = userBookStatus.map(UserBookStatus::getSentiment).orElse(null);

        return new BookDetailsDTO(book, totalLikes, likedByUser, status, sentiment);
    }

    /**
     * Fetches detailed book information from the Google Books API based on ISBN.
     *
     * @param isbn ISBN of the book
     * @return {@link VolumeInfo} object containing Google Books metadata
     */
    private VolumeInfo fetchVolumeInfoFromGoogle(String isbn) {
        String url = String.format(GOOGLE_BOOKS_API_URL, isbn, apiKey);
        GoogleBooksResponse response = restTemplate.getForObject(url, GoogleBooksResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found on Google Books for the provided ISBN.");
        }
        return response.items().getFirst().volumeInfo();
    }
}
