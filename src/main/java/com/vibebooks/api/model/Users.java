package com.vibebooks.api.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "users")
@Data // gera getters, setters, toString, equals e hashCode
@NoArgsConstructor // gera construtor vazio
@AllArgsConstructor // gera construtor com todos os campos
@Builder // opcional: permite usar o padrão Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // armazene o hash

    @Column(length = 500)
    private String bio;

    @Column(name = "created_at", nullable = false)
    @Builder.Default // usado para inicialização com Builder
    private LocalDateTime createdAt = LocalDateTime.now();
}

