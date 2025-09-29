package com.vibebooks.api.repository;

import com.vibebooks.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsBookByIsbn(String isbn);

    /**
     * Finds books where the title contains the given query string, ignoring case.
     * @param title The search query for the title.
     * @param pageable The pagination information.
     * @return A paginated list of books.
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
