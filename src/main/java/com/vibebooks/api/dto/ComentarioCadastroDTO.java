package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para receber os dados de cadastro de um novo comentário.
 */
public record ComentarioCadastroDTO(
    @NotBlank(message = "O comentário não pode estar em branco.")
    @Size(max = 1000, message = "O comentário não pode exceder 1000 caracteres.")
    String texto
) {
}