package com.vibebooks.api.model;

import com.vibebooks.api.dto.LivroCadastroDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "livros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autor;

    @Column(unique = true, length = 13)
    private String isbn;

    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;

    @Column(name = "url_capa")
    private String urlCapa;

    @CreationTimestamp
    @Column(name = "data_adicionado")
    private OffsetDateTime dataAdicionado;

    public Livro(LivroCadastroDTO dados) {
        this.titulo = dados.titulo();
        this.autor = dados.autor();
        this.isbn = dados.isbn();
        this.anoPublicacao = dados.anoPublicacao();
        this.urlCapa = dados.urlCapa();
    }

    public void atualizarInformacoes(LivroCadastroDTO dados) {
        if (dados.titulo() != null) {
            this.titulo = dados.titulo();
        }
        if (dados.autor() != null) {
            this.autor = dados.autor();
        }
        if (dados.isbn() != null) {
            this.isbn = dados.isbn();
        }
        if (dados.anoPublicacao() != null) {
            this.anoPublicacao = dados.anoPublicacao();
        }
        if (dados.urlCapa() != null) {
            this.urlCapa = dados.urlCapa();
        }
    }
}