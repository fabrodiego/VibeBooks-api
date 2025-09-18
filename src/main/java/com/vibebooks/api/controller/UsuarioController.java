package com.vibebooks.api.controller;


import com.vibebooks.api.dto.UsuarioCreateDTO;
import com.vibebooks.api.dto.UsuarioResponseDTO;
import com.vibebooks.api.model.Usuario;
import com.vibebooks.api.repository.UsuarioRepository;
import com.vibebooks.api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository ) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO criarUsuario(@RequestBody UsuarioCreateDTO dto){
        Usuario novoUsuario = usuarioService.criarUsuario(
                dto.nomeUsuario(),
                dto.email(),
                dto.senhaPura()
        );
        return new UsuarioResponseDTO(
                novoUsuario.getId(),
                novoUsuario.getNomeUsuario(),
                novoUsuario.getEmail()
        );
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioResponseDTO> dtos = usuarios.stream()
                .map(usuario -> new UsuarioResponseDTO(usuario.getId(), usuario.getNomeUsuario(), usuario.getEmail()))
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
