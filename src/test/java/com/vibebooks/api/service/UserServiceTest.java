package com.vibebooks.api.service;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.dto.UserPasswordUpdateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.dto.UserUpdateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UserService}.
 * Ensures business rules and validations are applied correctly.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /**
     * Global variables reused across multiple tests.
     */
    private UUID validId;
    private User validUser;
    private User loggedInUser;

    /**
     * Sets up the mock data before each test execution.
     */
    @BeforeEach
    void setup() {
        validId = UUID.randomUUID();

        validUser = new User();
        validUser.setId(validId);
        validUser.setUsername("test_user");
        validUser.setEmail("test@email.com");
        validUser.setPassword("hashed_password");

        loggedInUser = new User();
        loggedInUser.setId(validId);
    }

    /**
     * Tests the user creation flow.
     * Verifies that the password is encrypted before saving to the database.
     */
    @Test
    @DisplayName("Create: Should encrypt password and save user")
    void shouldEncryptPasswordAndSaveUser() {
        UserCreateDTO dto = new UserCreateDTO("new_user", "new@email.com", "123456");
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("HASH", result.getPassword());
        verify(userRepository).save(any());
    }

    /**
     * Tests the validation for existing users.
     * Verifies that a Conflict status is returned if the username or email is already taken.
     */
    @Test
    @DisplayName("Create: Should fail when user already exists")
    void shouldFailCreateWhenUserExists() {
        UserCreateDTO dto = new UserCreateDTO("new_user", "new@email.com", "123456");
        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(validUser));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.createUser(dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    /**
     * Tests the user update flow.
     * Verifies that valid data updates the user entity successfully.
     */
    @Test
    @DisplayName("Update: Should update user data successfully")
    void shouldUpdateUserSuccessfully() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("updated_name", "updated@email.com", "New bio");
        when(userRepository.findById(validId)).thenReturn(Optional.of(validUser));
        when(userRepository.findByUsername("updated_name")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@email.com")).thenReturn(Optional.empty());

        User result = userService.updateUser(validId, updateDTO, loggedInUser);

        assertEquals("updated_name", result.getUsername());
        assertEquals("updated@email.com", result.getEmail());
        assertEquals("New bio", result.getBio());
    }

    /**
     * Tests the authorization check during an update.
     * Verifies that a Forbidden status is returned if a user tries to update another user's profile.
     */
    @Test
    @DisplayName("Update: Should return Forbidden when trying to update another user")
    void shouldFailUpdateWhenNotAuthorized() {
        User hackerUser = new User();
        hackerUser.setId(UUID.randomUUID());

        UserUpdateDTO updateDTO = new UserUpdateDTO("name", "email", "bio");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(validId, updateDTO, hackerUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    /**
     * Tests the user deletion flow.
     * Verifies that the repository's delete method is called for a valid request.
     */
    @Test
    @DisplayName("Delete: Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(validId)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(validId, loggedInUser));
        verify(userRepository, times(1)).deleteById(validId);
    }

    /**
     * Tests the authorization check during deletion.
     * Verifies that the repository is never called if the user is not authorized.
     */
    @Test
    @DisplayName("Delete: Should return Forbidden when trying to delete another user")
    void shouldFailDeleteWhenNotAuthorized() {
        User hackerUser = new User();
        hackerUser.setId(UUID.randomUUID());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(validId, hackerUser));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(userRepository, never()).deleteById(any());
    }

    /**
     * Tests the retrieval of all users.
     * Verifies that the entity list is correctly mapped to a DTO list.
     */
    @Test
    @DisplayName("GetAll: Should return list of UserResponseDTO")
    void shouldReturnAllUsersMappedToDTO() {
        when(userRepository.findAll()).thenReturn(List.of(validUser));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(validId, result.getFirst().id());
        assertEquals("test_user", result.getFirst().username());
    }

    /**
     * Tests the password change flow.
     * Verifies that the new password is encrypted and saved if the old password matches.
     */
    @Test
    @DisplayName("Password: Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        UserPasswordUpdateDTO passDTO = new UserPasswordUpdateDTO("old_password", "new_password");
        when(userRepository.findById(validId)).thenReturn(Optional.of(validUser));

        when(passwordEncoder.matches("old_password", "hashed_password")).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("new_hashed_password");

        assertDoesNotThrow(() -> userService.changePassword(validId, passDTO, loggedInUser));
        assertEquals("new_hashed_password", validUser.getPassword());
    }

    /**
     * Tests the validation for incorrect old passwords.
     * Verifies that a Bad Request status is returned.
     */
    @Test
    @DisplayName("Password: Should fail when old password is incorrect")
    void shouldFailChangePasswordWhenOldPasswordIsWrong() {
        UserPasswordUpdateDTO passDTO = new UserPasswordUpdateDTO("wrong_password", "new_password");
        when(userRepository.findById(validId)).thenReturn(Optional.of(validUser));

        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.changePassword(validId, passDTO, loggedInUser));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}