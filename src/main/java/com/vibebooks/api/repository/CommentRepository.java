package com.vibebooks.api.repository;

import com.vibebooks.api.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    /**
     * Finds a page of comments for a specific book, fetching the associated user in a single query.
     * * @param bookId the UUID of the book.
     * @param pageable pagination information.
     * @return A Page of comments for the specified book.
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByBookId(UUID bookId, Pageable pageable);

    /**
     * Retrieves comments for multiple books in a single batch, fetching the associated user.
     * Resolves N+1 issues when building aggregated feeds.
     * * @param bookIds list of book UUIDs.
     * @return A list of comments associated with the provided book IDs.
     */
    @EntityGraph(attributePaths = {"user"})
    List<Comment> findAllByBookIdIn(List<UUID> bookIds);
}