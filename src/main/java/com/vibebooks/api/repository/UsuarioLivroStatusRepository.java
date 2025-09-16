package com.vibebooks.api.repository;

import com.vibebooks.api.model.UsuarioLivroStatus;
import com.vibebooks.api.model.UsuarioLivroStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioLivroStatusRepository extends JpaRepository<UsuarioLivroStatus, UsuarioLivroStatusId> {

}