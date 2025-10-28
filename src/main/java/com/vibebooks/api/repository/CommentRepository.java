package com.vibebooks.api.repository;

import com.vibebooks.api.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    /**
     * Finds a page of comments for a specific book.
     * @param bookId the "ID" of the book.
     * @param pageable pagination information (page number, size, sort).
     * @return A Page of comments for the specified book.
     */
    Page<Comment> findAllByBookId(UUID bookId, Pageable pageable);

    List<Comment> findAllByBookIdIn(List<UUID> bookIds);
}