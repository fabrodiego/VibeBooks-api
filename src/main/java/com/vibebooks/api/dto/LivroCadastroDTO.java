package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LivroCadastroDTO(
        @NotBlank(message = "O título não pode estar em branco.")
        String titulo,

        @NotBlank(message = "O nome do autor não pode estar em branco.")
        String autor,

        @Size(min = 10, max = 13, message = "O ISBN deve ter entre 10 e 13 caracteres.")
        String isbn,

        Integer anoPublicacao,

        String urlCapa
) {
}
