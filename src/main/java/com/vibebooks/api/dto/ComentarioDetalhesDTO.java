package com.vibebooks.api.dto;

import com.vibebooks.api.model.Comentario;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para exibir os detalhes de um comentário
 */
public record ComentarioDetalhesDTO(
        UUID id,
        String texto,
        String nomeUsuario,
        OffsetDateTime dataCriacao
) {
    /**
     * @param comentario a Entidade Comentario a ser convertida
     */
    public ComentarioDetalhesDTO(Comentario comentario) {
        this(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getUsuario().getNomeUsuario(),
                comentario.getDataCriacao()
        );
    }
}
