package com.vibebooks.api.repository;

import com.vibebooks.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    /**
     * Busca um usuário pelo seu nome de usuário.
     * @param nomeUsuario O nome de usuário a ser buscado.
     * @return Um Optional contendo o usuário se encontrado, ou um Optional vazio caso contrário.
     */
    Optional<Usuario> findByNomeUsuario(String nomeUsuario);

    /**
     * Busca um usuário pelo seu endereço de e-mail.
     * @param email O e-mail a ser buscado.
     * @return Um Optional contendo o usuário se encontrado, ou um Optional vazio caso contrário.
     */
    Optional<Usuario> findByEmail(String email);

}