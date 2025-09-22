package com.vibebooks.api.repository;

import com.vibebooks.api.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    /**
     * Finds all comments for a specific book.
     * @param bookId the "ID" of the book for which the comments will be fetched.
     * @return A list of comments for the specified book.
     */
    List<Comment> findAllByBookId(UUID bookId);
}