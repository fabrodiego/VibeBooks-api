package com.vibebooks.api.exception;

import com.vibebooks.api.dto.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the GlobalExceptionHandler.
 * Verifies that exceptions are correctly mapped to ErrorResponseDTO.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/test-path");
    }

    @Test
    @DisplayName("ResponseStatus: Should return standard error format")
    void shouldHandleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleResponseStatusException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Resource not found", response.getBody().message());
        assertEquals("/test-path", response.getBody().path());
    }

    @Test
    @DisplayName("Validation: Should extract messages from MethodArgumentNotValidException")
    void shouldHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "title", "Title cannot be blank");
        FieldError fieldError2 = new FieldError("object", "isbn", "ISBN is invalid");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationExceptions(ex, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Title cannot be blank; ISBN is invalid", response.getBody().message());
        assertEquals("/test-path", response.getBody().path());
    }

    @Test
    @DisplayName("Generic: Should hide internal details on Exception")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Database connection failed completely");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("An unexpected error occurred.", response.getBody().message());
        assertEquals("/test-path", response.getBody().path());
    }
}