package com.vibebooks.api.repository;

import com.vibebooks.api.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsBookByIsbn(String isbn);
}