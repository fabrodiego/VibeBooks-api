package com.vibebooks.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_book_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBookStatus {

    @EmbeddedId
    private UserBookStatusId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    private Book book;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private ReadingStatus status = ReadingStatus.WANT_TO_READ;

    @Column(nullable = false)
    private boolean saved = false;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sentiment")
    private BookSentiment sentiment;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "liked", nullable = false)
    private boolean liked = false;

}