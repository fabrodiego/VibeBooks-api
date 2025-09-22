package com.vibebooks.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "status_leitura")
    private StatusLeitura status = StatusLeitura.QUERO_LER;

    @Column(nullable = false)
    private boolean salvo = false;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "sentimento_livro")
    private SentimentoLivro sentimento;

    @org.hibernate.annotations.UpdateTimestamp
    @Column(name = "data_atualizacao")
    private OffsetDateTime dataAtualizacao;

    @Column(name = "curtido", nullable = false)
    private boolean curtido = false;

}