package com.vibebooks.api.repository;

import com.vibebooks.api.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    List<Comentario> findByLivroId(UUID livroId);
}