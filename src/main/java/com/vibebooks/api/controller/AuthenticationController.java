package com.vibebooks.api.controller;

import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.TokenDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/login")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<TokenDTO> login(@RequestBody AuthenticationDTO dto) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                dto.login(),
                dto.password()
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User authenticatedUser = (User) authentication.getPrincipal();

        String token = tokenService.generateToken(authenticatedUser);

        return ResponseEntity.ok(new TokenDTO(token));
    }
}
