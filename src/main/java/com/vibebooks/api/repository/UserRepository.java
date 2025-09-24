package com.vibebooks.api.repository;

import com.vibebooks.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their username or email address.
     * @param username The username of the user to find.
     * @param email The email address of the user to find.
     * @return An Optional containing the found User, or an empty Optional if no user is found.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

}