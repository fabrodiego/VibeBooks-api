package com.vibebooks.api.repository;

import com.vibebooks.api.model.UserBookStatus;
import com.vibebooks.api.model.UserBookStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBookStatusRepository extends JpaRepository<UserBookStatus, UserBookStatusId> {

    long countByBookIdAndLikedIsTrue(UUID bookId);

    @Transactional
    void deleteByBookId(UUID bookId);
}