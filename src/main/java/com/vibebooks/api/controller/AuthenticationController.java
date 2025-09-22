package com.vibebooks.api.controller;

import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.TokenDTO;
import com.vibebooks.api.model.User;
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
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<TokenDTO> login(@RequestBody AuthenticationDTO dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User authenticatedUser = (User) authentication.getPrincipal();

        String token = tokenService.generateToken(authenticatedUser);

        return ResponseEntity.ok(new TokenDTO(token));
    }
}
