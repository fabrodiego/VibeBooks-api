package com.vibebooks.api.controller;


import com.vibebooks.api.dto.UsuarioCreateDTO;
import com.vibebooks.api.dto.UsuarioResponseDTO;
import com.vibebooks.api.model.Usuario;
import com.vibebooks.api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService ) {
        this.usuarioService = usuarioService;
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
}
