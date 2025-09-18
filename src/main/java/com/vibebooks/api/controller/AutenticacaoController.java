package com.vibebooks.api.controller;

import com.vibebooks.api.dto.AutenticacaoDTO;
import com.vibebooks.api.dto.TokenDTO;
import com.vibebooks.api.model.Usuario;
import com.vibebooks.api.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<TokenDTO> login(@RequestBody AutenticacaoDTO dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        String token = tokenService.gerarToken(usuarioAutenticado);

        return ResponseEntity.ok(new TokenDTO(token));
    }
}
