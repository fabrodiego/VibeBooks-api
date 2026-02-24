package com.vibebooks.api.service;

import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthenticationService.
 * Verifies user retrieval logic for the Spring Security context.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User validUser;

    @BeforeEach
    void setup() {
        validUser = new User();
        validUser.setUsername("test_user");
        validUser.setEmail("test@email.com");
        validUser.setPassword("hashed_password");
    }

    /**
     * Tests successful retrieval of a user by email or username.
     */
    @Test
    @DisplayName("LoadUser: Should return UserDetails when user exists")
    void shouldLoadUserByUsernameSuccessfully() {
        when(userRepository.findByUsernameOrEmail("test@email.com", "test@email.com"))
                .thenReturn(Optional.of(validUser));

        UserDetails result = authenticationService.loadUserByUsername("test@email.com");

        assertNotNull(result);
        assertEquals("test_user", result.getUsername());
        assertEquals("hashed_password", result.getPassword());
        verify(userRepository, times(1)).findByUsernameOrEmail("test@email.com", "test@email.com");
    }

    /**
     * Tests the exception thrown when a user does not exist in the database.
     */
    @Test
    @DisplayName("LoadUser: Should throw UsernameNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.loadUserByUsername("unknown"));

        verify(userRepository, times(1)).findByUsernameOrEmail("unknown", "unknown");
    }
}