package com.vibebooks.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "followers")
@Getter
@Setter
@NoArgsConstructor
public class Follow {

    @EmbeddedId
    private FollowId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private User following;

    @CreationTimestamp
    @Column(name = "followed_at", nullable = false, updatable = false)
    private OffsetDateTime followedAt;


    /**
     * Convenience constructor to create a new 'Follow' relationship.
     * Contains the logic to build the composite ID from the entities.
     *
     * @param follower The user who follows.
     * @param following The user who is being followed.
     */
    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
        this.id = new FollowId(follower.getId(), following.getId());
    }
}
