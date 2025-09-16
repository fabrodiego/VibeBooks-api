package com.vibebooks.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_livro_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLivroStatus {

    @EmbeddedId
    private UsuarioLivroStatusId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("livroId")
    private Livro livro;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status_leitura")
    private StatusLeitura status;

    private boolean salvo;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "sentimento_livro")
    private SentimentoLivro sentimento;

    @Column(name = "data_atualizacao", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime dataAtualizacao;
}