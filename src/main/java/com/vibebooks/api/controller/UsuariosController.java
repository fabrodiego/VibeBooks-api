package com.vibebooks.api.controller;


import com.vibebooks.api.dtos.UsuariosDTO;
import com.vibebooks.api.model.Usuarios;
import com.vibebooks.api.repository.UsuariosRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsuariosController {

    private final UsuariosRepository usuariosRepository;

    public UsuariosController(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    @GetMapping
    public ResponseEntity<List<Usuarios>> listarTodosUsuarios() {
        List<Usuarios> todosUsuarios = usuariosRepository.findAll();
        return ResponseEntity.ok(todosUsuarios);
    }

    @PostMapping
    public ResponseEntity<Usuarios> criarUsuario(@RequestBody UsuariosDTO usuariosDto) {
        var novoUsuario = new Usuarios();
        BeanUtils.copyProperties(usuariosDto, novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuariosRepository.save(novoUsuario));

    }
}
