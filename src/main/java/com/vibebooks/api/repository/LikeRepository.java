package com.vibebooks.api.repository;

import com.vibebooks.api.model.Like;
import com.vibebooks.api.model.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    /**
     * Finds a specific like by the combination of the user ID and the comment ID.
     * This method will be crucial to know if a user has already liked a comment,
     * allowing for the like/unlike logic.
     *
     * @param userId The user's "ID".
     * @param commentId The comment's "ID".
     * @return An Optional containing the Like if it exists, or empty otherwise.
     */
    Optional<Like> findByUserIdAndCommentId(UUID userId, UUID commentId);
}
