package com.vibebooks.api.repository;

import com.vibebooks.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    /**
     * Efficiently checks if a book with the given ISBN exists.
     * Best for validation checks.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Finds a book entity by its ISBN.
     * Useful when the full book object is needed.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Finds books where the title contains the given query string, ignoring case.
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}