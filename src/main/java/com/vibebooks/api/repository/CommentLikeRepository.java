package com.vibebooks.api.repository;

import com.vibebooks.api.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    /**
     * Finds a specific like by the combination of the user ID and the comment ID.
     * This method will be crucial to know if a user has already liked a comment,
     * allowing for the like/unlike logic.
     *
     * @param userId The user's "ID".
     * @param commentId The comment's "ID".
     * @return An Optional containing the Like if it exists, or empty otherwise.
     */
    Optional<CommentLike> findByUserIdAndCommentId(UUID userId, UUID commentId);

    /**
     * Counts the total number of "likes" (entries) for a specific comment.
     *
     * @param commentId The "ID" of the comment.
     * @return The total number of likes (long).
     */
    long countByCommentId(UUID commentId);
}
