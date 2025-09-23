package com.vibebooks.api.repository;

import com.vibebooks.api.model.Follow;
import com.vibebooks.api.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    /**
     * Finds all records where the specified user is the follower.
     *
     * @param followerId The ID of the user who is following.
     * @return A list of 'Follow' relationships.
     */
    List<Follow> findAllByFollowerId(UUID followerId);

    /**
     * Finds all records where the specified user is being followed.
     *
     * @param followingId The ID of the user who is being followed.
     * @return A list of 'Follow' relationships.
     */
    List<Follow> findAllByFollowingId(UUID followingId);

    /**
     * Efficiently checks if a "follow" relationship already exists.
     *
     * @param followerId The ID of the user who is following.
     * @param followingId The ID of the user who is being followed.
     * @return `true` if the relationship exists, `false` otherwise.
     */
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

}