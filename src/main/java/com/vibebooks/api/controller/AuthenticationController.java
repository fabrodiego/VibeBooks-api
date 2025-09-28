package com.vibebooks.api.controller;

import com.vibebooks.api.dto.AuthenticationDTO;
import com.vibebooks.api.dto.TokenDTO;
import com.vibebooks.api.dto.UserCreateDTO;
import com.vibebooks.api.dto.UserResponseDTO;
import com.vibebooks.api.model.User;
import com.vibebooks.api.service.TokenService;
import com.vibebooks.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.vibebooks.api.util.ApiConstants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
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

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(@RequestBody UserCreateDTO dto){
        User newUser = userService.createUser(dto);
        return new UserResponseDTO(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail()
        );
    }
}
