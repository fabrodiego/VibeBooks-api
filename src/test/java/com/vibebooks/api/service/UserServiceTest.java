package com.vibebooks.api.service;

import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Unitário: Deve criptografar a senha antes de salvar")
    void shouldEncryptPasswordBeforeSaving() {
        UserCreateDTO dto = new UserCreateDTO("user", "email@test.com", "123456");

        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.empty());

        when(passwordEncoder.encode("123456")).thenReturn("SENHA_CRIPTOGRAFADA_HASH");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(dto);

        assertNotNull(createdUser);
        assertEquals("SENHA_CRIPTOGRAFADA_HASH", createdUser.getPassword());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Unitário: Deve lançar erro se usuário já existir")
    void shouldThrowErrorIfUserAlreadyExists() {
        UserCreateDTO dto = new UserCreateDTO("user", "email@test.com", "123");

        when(userRepository.findByUsernameOrEmail(any(), any())).thenReturn(Optional.of(new User()));

        assertThrows(ResponseStatusException.class, () -> userService.createUser(dto));

        verify(userRepository, never()).save(any());
    }
}