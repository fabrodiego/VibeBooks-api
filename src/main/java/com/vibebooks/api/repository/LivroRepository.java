package com.vibebooks.api.repository;

import com.vibebooks.api.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface LivroRepository extends JpaRepository<Livro, UUID> {

    Optional<Livro> findByIsbn(String isbn);
}