package com.vibebooks.api.dto;

import com.vibebooks.api.model.Livro;
import java.util.UUID;

public record LivroDetalhesDTO(
        UUID id,
        String titulo,
        String autor,
        String isbn,
        Integer anoPublicacao,
        String urlCapa
) {

    // Um construtor adicional ser mais prática a conversão da entidade Livro para este DTO.
    public LivroDetalhesDTO(Livro livro) {
        this(livro.getId(), livro.getTitulo(), livro.getAutor(), livro.getIsbn(),  livro.getAnoPublicacao(), livro.getUrlCapa());
    }
}
