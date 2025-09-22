package com.vibebooks.api.repository;

import com.vibebooks.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return An Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByEmail(String email);

}