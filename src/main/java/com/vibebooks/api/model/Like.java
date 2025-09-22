package com.vibebooks.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    @EmbeddedId
    private LikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    private Comment comment;

    @CreationTimestamp
    @Column(name = "creation_date")
    private OffsetDateTime creationDate;

    public Like(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
        this.id = new LikeId(user.getId(), comment.getId());
    }
}