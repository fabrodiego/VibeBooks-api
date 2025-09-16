package com.vibebooks.api.repository;

import com.vibebooks.api.model.Curtida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CurtidaRepository extends JpaRepository<Curtida, UUID> {

}
