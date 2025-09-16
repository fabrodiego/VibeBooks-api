package com.vibebooks.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "curtidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Curtida {

    @EmbeddedId
    private CurtidaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("comentarioId")
    private Comentario comentario;

    @Column(name = "data_criacao", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime dataCriacao;

}