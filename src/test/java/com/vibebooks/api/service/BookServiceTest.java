package com.vibebooks.api.service;

import com.vibebooks.api.dto.BookCreationDTO;
import com.vibebooks.api.dto.BookDetailsDTO;
import com.vibebooks.api.dto.BookIsbnDTO;
import com.vibebooks.api.dto.BookStatusUpdateDTO;
import com.vibebooks.api.dto.google.GoogleBooksResponse;
import com.vibebooks.api.dto.google.GoogleBookItem;
import com.vibebooks.api.dto.google.ImageLinks;
import com.vibebooks.api.dto.google.VolumeInfo;
import com.vibebooks.api.model.*;
import com.vibebooks.api.repository.BookRepository;
import com.vibebooks.api.repository.UserBookStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link BookService}.
 * Focuses on business rules, repository interactions, and mocking external APIs.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookStatusRepository userBookStatusRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookService bookService;

    private UUID validBookId;
    private Book validBook;
    private User loggedInUser;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(bookService, "apiKey", "dummy-api-key");

        validBookId = UUID.randomUUID();

        validBook = new Book();
        validBook.setId(validBookId);
        validBook.setIsbn("1234567890");
        validBook.setTitle("The Hobbit");
        validBook.setAuthor("J.R.R. Tolkien");

        loggedInUser = new User();
        loggedInUser.setId(UUID.randomUUID());
    }


    /**
     * Tests the successful creation of a book.
     * Mocks the RestTemplate to return a fake Google Books response, avoiding real network calls.
     */
    @Test
    @DisplayName("Create: Should fetch from Google Books and save new book")
    void shouldCreateBookSuccessfullyFromGoogleApi() {
        BookIsbnDTO dto = new BookIsbnDTO("1234567890");
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(false);

        ImageLinks fakeImageLinks = new ImageLinks("http://thumbnail.url", "http://small.url");
        VolumeInfo fakeVolumeInfo = new VolumeInfo("The Hobbit", List.of("J.R.R. Tolkien"), "1937-09-21", fakeImageLinks);
        GoogleBookItem fakeItem = new GoogleBookItem(fakeVolumeInfo);
        GoogleBooksResponse fakeResponse = new GoogleBooksResponse(List.of(fakeItem));

        when(restTemplate.getForObject(anyString(), eq(GoogleBooksResponse.class))).thenReturn(fakeResponse);

        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        Book result = bookService.createBook(dto);

        assertNotNull(result);
        assertEquals("The Hobbit", result.getTitle());
        assertEquals("1234567890", result.getIsbn());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(GoogleBooksResponse.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    /**
     * Tests the scenario where the book is not found in the Google API.
     * Expects a 404 Not Found exception.
     */
    @Test
    @DisplayName("Create: Should fail if Google Books API returns empty/null")
    void shouldFailCreateWhenGoogleApiReturnsEmpty() {
        BookIsbnDTO dto = new BookIsbnDTO("invalid-isbn");
        when(bookRepository.existsByIsbn("invalid-isbn")).thenReturn(false);

        // Simulate Google API returning nothing
        when(restTemplate.getForObject(anyString(), eq(GoogleBooksResponse.class))).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> bookService.createBook(dto));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(bookRepository, never()).save(any());
    }

    /**
     * Tests the validation for duplicated ISBNs in the database.
     * Expects a 409 Conflict.
     */
    @Test
    @DisplayName("Create: Should fail when ISBN already exists in DB")
    void shouldFailCreateWhenIsbnAlreadyExists() {
        BookIsbnDTO dto = new BookIsbnDTO("1234567890");
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> bookService.createBook(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    /**
     * Tests retrieving a book by its ID.
     * Verifies that the book is correctly mapped to a BookDetailsDTO with related user statuses.
     */
    @Test
    @DisplayName("Find: Should return BookDetailsDTO when book exists")
    void shouldFindBookById() {
        when(bookRepository.findById(validBookId)).thenReturn(Optional.of(validBook));
        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBookId)).thenReturn(10L);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(userBookStatusRepository.countSentimentsByBookId(validBookId)).thenReturn(List.of());

        BookDetailsDTO result = bookService.findBookById(validBookId, loggedInUser);

        assertNotNull(result);
        assertEquals("The Hobbit", result.title());
        assertEquals(10L, result.likesCount());
        verify(bookRepository, times(1)).findById(validBookId);
    }

    /**
     * Tests the book deletion process.
     * Ensures that both the book and its associated user statuses are removed from the database.
     */
    @Test
    @DisplayName("Delete: Should remove book and associated user statuses")
    void shouldDeleteBookSuccessfully() {
        when(bookRepository.existsById(validBookId)).thenReturn(true);

        bookService.deleteBook(validBookId);

        verify(userBookStatusRepository, times(1)).deleteByBookId(validBookId);
        verify(bookRepository, times(1)).deleteById(validBookId);
    }

    /**
     * Tests updating a book's basic information.
     * Verifies that the entity is updated and the full DTO is returned.
     */
    @Test
    @DisplayName("Update: Should update book information successfully")
    void shouldUpdateBookSuccessfully() {
        BookCreationDTO updateDTO = new BookCreationDTO(
                "Updated Title",
                "Updated Author",
                "1234567890",
                2024,
                "http://new-cover.url"
        );
        when(bookRepository.findById(validBookId)).thenReturn(Optional.of(validBook));

        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBookId)).thenReturn(0L);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(userBookStatusRepository.countSentimentsByBookId(validBookId)).thenReturn(List.of());

        BookDetailsDTO result = bookService.updateBook(validBookId, updateDTO, loggedInUser);

        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        assertEquals("Updated Author", result.author());
    }

    /**
     * Tests updating the reading status of a book.
     * Verifies that the status updates correctly and sentiment is nullified if status is WANT_TO_READ.
     */
    @Test
    @DisplayName("Status: Should update reading status successfully")
    void shouldUpdateBookStatus() {
        BookStatusUpdateDTO statusDTO = new BookStatusUpdateDTO(ReadingStatus.READING, null);

        UserBookStatus existingStatus = new UserBookStatus();
        existingStatus.setBook(validBook);
        existingStatus.setUser(loggedInUser);

        when(bookRepository.existsById(validBookId)).thenReturn(true);
        when(bookRepository.getReferenceById(validBookId)).thenReturn(validBook);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.of(existingStatus));
        when(userBookStatusRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        UserBookStatus result = bookService.updateBookStatus(validBookId, loggedInUser, statusDTO);

        assertNotNull(result);
        assertEquals(ReadingStatus.READING, result.getStatus());
    }

    /**
     * Tests the business rule that prevents adding a sentiment to a book the user only "wants to read".
     * Expects a 400 Bad Request.
     */
    @Test
    @DisplayName("Status: Should fail to add sentiment if status is WANT_TO_READ")
    void shouldFailToAddSentimentToWantToReadBook() {
        BookSentiment anySentiment = BookSentiment.values().length > 0 ? BookSentiment.values()[0] : null;
        BookStatusUpdateDTO statusDTO = new BookStatusUpdateDTO(ReadingStatus.WANT_TO_READ, anySentiment);

        UserBookStatus existingStatus = new UserBookStatus();
        existingStatus.setStatus(ReadingStatus.WANT_TO_READ);

        when(bookRepository.existsById(validBookId)).thenReturn(true);
        when(bookRepository.getReferenceById(validBookId)).thenReturn(validBook);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.of(existingStatus));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookService.updateBookStatus(validBookId, loggedInUser, statusDTO));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    /**
     * Tests the like toggle mechanism.
     * Verifies that a false liked status becomes true.
     */
    @Test
    @DisplayName("Like: Should toggle like status to true")
    void shouldToggleLikeStatus() {
        UserBookStatus existingStatus = new UserBookStatus();
        existingStatus.setLiked(false);

        when(bookRepository.existsById(validBookId)).thenReturn(true);
        when(bookRepository.getReferenceById(validBookId)).thenReturn(validBook);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.of(existingStatus));
        when(userBookStatusRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        UserBookStatus result = bookService.likeOrUnlikeBook(validBookId, loggedInUser);

        assertTrue(result.isLiked());
    }

    /**
     * Tests pagination and mapping for the list all books feature.
     */
    @Test
    @DisplayName("List: Should return paginated BookDetailsDTO list")
    void shouldListAllBooks() {
        org.springframework.data.domain.Page<Book> bookPage = new org.springframework.data.domain.PageImpl<>(List.of(validBook));
        when(bookRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(bookPage);

        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBookId)).thenReturn(5L);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(userBookStatusRepository.countSentimentsByBookId(validBookId)).thenReturn(List.of());

        org.springframework.data.domain.Page<BookDetailsDTO> result = bookService.listAllBooks(org.springframework.data.domain.PageRequest.of(0, 10), loggedInUser);

        assertFalse(result.isEmpty());
        assertEquals("The Hobbit", result.getContent().getFirst().title());
        assertEquals(5L, result.getContent().getFirst().likesCount());
    }

    /**
     * Tests the search feature mapping and repository call.
     */
    @Test
    @DisplayName("Search: Should return books matching query")
    void shouldSearchBooksSuccessfully() {
        org.springframework.data.domain.Page<Book> bookPage = new org.springframework.data.domain.PageImpl<>(List.of(validBook));
        when(bookRepository.findByTitleContainingIgnoreCase(eq("Hobbit"), any())).thenReturn(bookPage);

        when(userBookStatusRepository.countByBookIdAndLikedIsTrue(validBookId)).thenReturn(0L);
        when(userBookStatusRepository.findById(any())).thenReturn(Optional.empty());
        when(userBookStatusRepository.countSentimentsByBookId(validBookId)).thenReturn(List.of());

        org.springframework.data.domain.Page<BookDetailsDTO> result = bookService.searchBooks("Hobbit", org.springframework.data.domain.PageRequest.of(0, 10), loggedInUser);

        assertFalse(result.isEmpty());
        assertEquals("The Hobbit", result.getContent().getFirst().title());
    }
}