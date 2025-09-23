package com.vibebooks.api.exception;

import com.vibebooks.api.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        var errorDTO = new ErrorResponseDTO(
                (HttpStatus) ex.getStatusCode(),
                ex.getReason(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDTO, ex.getStatusCode());
    }
}