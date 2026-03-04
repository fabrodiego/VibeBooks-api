package com.vibebooks.api.repository;

import com.vibebooks.api.dto.SentimentCountDTO;
import com.vibebooks.api.model.UserBookStatus;
import com.vibebooks.api.model.UserBookStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserBookStatusRepository extends JpaRepository<UserBookStatus, UserBookStatusId> {

    long countByBookIdAndLikedIsTrue(UUID bookId);

    @Query("""
            SELECT new com.vibebooks.api.dto.SentimentCountDTO(ubs.sentiment, COUNT(ubs.id))
            FROM UserBookStatus ubs
            WHERE ubs.book.id = :bookId
            AND ubs.sentiment IS NOT NULL
            GROUP BY ubs.sentiment
            """)
    List<SentimentCountDTO> countSentimentsByBookId(@Param("bookId") UUID bookId);

    @Transactional
    void deleteByBookId(UUID bookId);


    List<UserBookStatus> findAllByUserIdAndBookIdIn(UUID userId, List<UUID> bookIds);

    interface BookLikeCount {
        UUID getBookId();
        long getCount();
    }

    @Query("""
            SELECT ubs.book.id as bookId, COUNT(ubs.id) as count
            FROM UserBookStatus ubs
            WHERE ubs.book.id IN :bookIds AND ubs.liked = true
            GROUP BY ubs.book.id
            """)
    List<BookLikeCount> countLikesByBookIdIn(@Param("bookIds") List<UUID> bookIds);

    interface BookSentimentAggregation {
        UUID getBookId();
        com.vibebooks.api.model.BookSentiment getSentiment();
        long getCount();
    }

    @Query("""
            SELECT ubs.book.id as bookId, ubs.sentiment as sentiment, COUNT(ubs.id) as count
            FROM UserBookStatus ubs
            WHERE ubs.book.id IN :bookIds AND ubs.sentiment IS NOT NULL
            GROUP BY ubs.book.id, ubs.sentiment
            """)
    List<BookSentimentAggregation> countSentimentsByBookIdIn(@Param("bookIds") List<UUID> bookIds);
}